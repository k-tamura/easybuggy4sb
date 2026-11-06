FROM ollama/ollama:0.12.8

# Receive MODEL_NAME from args in docker-compose.yml
#ARG MODEL_NAME=qwen:0.5b
ARG MODEL_NAME=llama3.2:1b

# Download the specified model and store it in the image
RUN ollama serve & \
    sleep 5 && \
    ollama pull ${MODEL_NAME}