from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional, List
import jieba
import logging
from transformers import AutoTokenizer, AutoModelForSeq2SeqLM, pipeline
from threading import Lock
import time

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Transformer 模型配置与懒加载
MODEL_NAME = "uer/t5-small-chinese-cluecorpussmall"
_summarizer = None
_model_lock = Lock()
model_loaded = False


def get_summarizer():
    global _summarizer, model_loaded
    if _summarizer is None:
        with _model_lock:
            if _summarizer is None:
                _summarizer = pipeline("summarization", model=MODEL_NAME, tokenizer=MODEL_NAME)
                model_loaded = True
    return _summarizer


app = FastAPI(
    title="学习推荐系统-AI服务",
    description="提供摘要生成和文本处理服务",
    version="1.0.0"
)

# 跨域配置
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 请求模型
class SummarizeRequest(BaseModel):
    text: str
    max_length: Optional[int] = 150
    min_length: Optional[int] = 50
    summary_type: Optional[str] = "extractive"  # extractive/abstractive

# 响应模型
class SummarizeResponse(BaseModel):
    summary: str
    key_points: List[str]
    keywords: List[str]
    model: str
    duration: float

@app.get("/")
async def root():
    return {"message": "学习推荐系统AI服务运行中", "status": "ok"}

@app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "model_loaded": model_loaded,
        "gpu_available": False
    }

@app.post("/api/summarize", response_model=SummarizeResponse)
async def generate_summary(request: SummarizeRequest):
    """生成摘要接口，优先使用 Transformer 模型，失败时回退到简单规则"""
    start_time = time.time()
    text = request.text

    if len(text) < 50:
        raise HTTPException(status_code=400, detail="文本长度太短,至少需要50字")

    try:
        # 优先使用基于 Transformer 的摘要模型
        summary = None
        try:
            summarizer = get_summarizer()
            result = summarizer(
                text,
                max_length=request.max_length,
                min_length=request.min_length,
                do_sample=False
            )
            if result and isinstance(result, list):
                summary = result[0].get("summary_text") or result[0].get("generated_text")
        except Exception as e:
            logger.warning(f"Transformer 摘要失败，将回退到简单算法: {e}")

        # 回退方案：简单分句 + 取前几句
        if not summary:
            sentences = [s.strip() for s in text.replace('。', '。\n').split('\n') if s.strip()]
            summary = '。'.join(sentences[:3]) + '。'
            key_points = sentences[:3] if len(sentences) >= 3 else sentences
        else:
            # 若使用了 Transformer，则按摘要再次抽取关键句
            sentences = [s.strip() for s in text.replace('。', '。\n').split('\n') if s.strip()]
            key_points = sentences[:3] if len(sentences) >= 3 else sentences

        # 提取关键词
        keywords = list(jieba.analyse.extract_tags(text, topK=5))

        duration = time.time() - start_time

        return SummarizeResponse(
            summary=summary,
            key_points=key_points,
            keywords=keywords,
            model=MODEL_NAME if model_loaded else "TextRank(临时)",
            duration=round(duration, 2)
        )

    except Exception as e:
        logger.error(f"摘要生成失败: {str(e)}")
        raise HTTPException(status_code=500, detail=f"摘要生成失败: {str(e)}")

@app.post("/api/keywords")
async def extract_keywords(text: str, top_k: int = 10):
    """提取关键词"""
    try:
        keywords = jieba.analyse.extract_tags(text, topK=top_k, withWeight=True)
        return {
            "code": 200,
            "data": [{"word": word, "weight": weight} for word, weight in keywords]
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# ============================================
# 问答模块接口
# ============================================

class QARequest(BaseModel):
    question: str

class KnowledgeGenerateRequest(BaseModel):
    question: str
    answer: str

@app.post("/api/qa/answer")
async def answer_question(request: QARequest):
    """回答问题接口（临时实现）"""
    try:
        question = request.question
        
        # TODO: 集成真实大模型（通义千问/ChatGLM）
        # 临时实现：简单规则回答
        answer = f"根据您的问题'{question}'，这是一个关于学习的知识点。建议您深入研究相关资料，理解核心概念和应用场景。如需更详细的解答，请提供更多上下文信息。"
        
        return {
            "answer": answer,
            "model": "rule-based(临时)",
            "confidence": 0.7
        }
    except Exception as e:
        logger.error(f"问答失败: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/qa/generate-knowledge")
async def generate_knowledge_point(request: KnowledgeGenerateRequest):
    """从问答生成知识点"""
    try:
        question = request.question
        answer = request.answer
        
        # 提取关键词作为标签
        keywords = list(jieba.analyse.extract_tags(question + answer, topK=5))
        
        # 简单生成知识点结构
        knowledge_point = {
            "title": question[:50] if len(question) <= 50 else question[:47] + "...",
            "category": "计算机",  # TODO: 智能分类
            "sub_category": keywords[0] if keywords else "其他",
            "description": answer[:100] if len(answer) <= 100 else answer[:97] + "...",
            "tags": keywords,
            "difficulty": "中级"
        }
        
        return {
            "knowledge_point": knowledge_point,
            "model": "TextRank(临时)"
        }
    except Exception as e:
        logger.error(f"生成知识点失败: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5000)
