# 1) Ir para a pasta do projeto
cd /mnt/c/Users/Wesley\ Eduardo/Documents/projetos-git/devquote-backend

# 2) Definir imagem e tag com data-hora-segundo
IMAGE=wesleyeduardodev/devquote-backend
TAG=$(date +%d-%m-%Y-%H-%M-%S)

# 3) Login no Docker Hub (modo interativo)
docker login -u SEU_USUARIO

# 4) Build da imagem com tag única e latest
docker build -t $IMAGE:$TAG -t $IMAGE:latest .

# 5) Push da tag única
docker push $IMAGE:$TAG

# 6) Push do latest (opcional, mas recomendado)
docker push $IMAGE:latest

# 7) Mostrar a URL para usar no Render
echo "docker.io/$IMAGE:$TAG"
