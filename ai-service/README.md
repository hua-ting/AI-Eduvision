# AI服务启动说明

## 环境准备

### 1. 安装Python (需要3.9+)
```bash
python --version
```

### 2. 创建虚拟环境(可选但推荐)
```bash
python -m venv venv

# Windows激活
venv\Scripts\activate

# Linux/Mac激活
source venv/bin/activate
```

### 3. 安装依赖
```bash
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
```

## 启动服务

```bash
python main.py
```

服务将运行在: http://localhost:5000

## API文档

启动后访问: http://localhost:5000/docs

## 接口说明

### 1. 健康检查
GET /health

### 2. 生成摘要
POST /api/summarize
```json
{
  "text": "原文内容...",
  "max_length": 150,
  "min_length": 50,
  "summary_type": "extractive"
}
```

### 3. 提取关键词
POST /api/keywords?text=文本内容&top_k=10

## 后续优化

当前使用TextRank临时方案,后续需要:
1. 下载T5-Pegasus模型
2. 集成模型推理代码
3. 优化生成效果
