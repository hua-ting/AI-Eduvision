# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a learning recommendation system based on Transformer models for personalized learning material summarization and recommendation. The system consists of three main components:

1. **Frontend**: Vue 3 + Vite application with Naive UI components
2. **Backend**: Spring Boot Java application with MySQL database
3. **AI Service**: Python FastAPI service for NLP tasks (summarization, keyword extraction)

## Repository Structure

```
biyesheji/
├── frontend/           # Vue 3 + Vite frontend
├── backend/            # Spring Boot backend
├── ai-service/         # Python AI service for NLP
└── database/           # Database initialization scripts
```

## Development Commands

### Frontend (Vue 3 + Vite)

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

### Backend (Spring Boot)

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run

# Or run the built JAR
java -jar target/recommend-system-1.0.0.jar
```

### AI Service (Python)

```bash
# Install dependencies
pip install -r requirements.txt

# Start the service
python main.py
```

## Architecture Overview

### Frontend Architecture
- **Framework**: Vue 3 with Composition API
- **State Management**: Pinia
- **Routing**: Vue Router
- **UI Library**: Naive UI
- **Build Tool**: Vite
- **Proxy**: Configured to proxy `/api` requests to backend at `http://localhost:8080`

### Backend Architecture
- **Framework**: Spring Boot 2.7.18
- **Database**: MySQL with MyBatis-Plus ORM
- **Security**: JWT-based authentication
- **Documentation**: Knife4j (OpenAPI 2)
- **External Services**: Integration with AI service via REST API
- **Crawling**: JSoup-based web crawler for learning materials

### AI Service Architecture
- **Framework**: FastAPI
- **NLP Models**: Transformers library with T5-Pegasus model
- **Tokenization**: Jieba for Chinese text processing
- **Fallback**: Rule-based algorithms when models fail

## Key Features

1. **User Management**: Student and admin roles with JWT authentication
2. **Knowledge Points**: Structured learning content with categories and difficulty levels
3. **Learning Materials**: Document management with AI-generated summaries
4. **Personalized Recommendations**: Content-based and collaborative filtering algorithms
5. **AI-Powered Q&A**: Natural language question answering with knowledge point generation
6. **User Profiling**: Learning behavior tracking and preference analysis
7. **Content Crawling**: Automated collection of learning materials from external sources

## Database Schema

Main tables:
- `t_user`: User accounts and roles
- `t_user_profile`: Detailed user learning profiles
- `t_knowledge_point`: Structured learning content
- `t_material`: Learning documents and resources
- `t_material_summary`: AI-generated document summaries
- `t_user_behavior`: Tracking of user interactions
- `t_user_collection`: User bookmarked content
- `t_qa_record`: Question-answering history

## API Documentation

- **Backend**: http://localhost:8080/api/doc.html (Knife4j)
- **AI Service**: http://localhost:5000/docs (FastAPI Swagger)

## Default Credentials

- **Admin**: username: `admin`, password: `admin123`
- **Students**: usernames: `student01` through `student20`, password: `123456` for all

## Development Notes

1. **Cross-Origin Configuration**: Frontend proxies to backend, backend has CORS enabled
2. **AI Integration**: Backend calls AI service for summarization and Q&A features
3. **Component Libraries**: Heavy use of Naive UI components with auto-import
4. **Database**: MySQL required with UTF8MB4 charset for emoji support
5. **Testing Data**: Pre-populated with sample users and content