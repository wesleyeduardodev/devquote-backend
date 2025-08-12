docker stop $(docker ps -aq) 2>/dev/null
docker rm -vf $(docker ps -aq) 2>/dev/null
docker rmi -f $(docker images -aq) 2>/dev/null
docker volume rm $(docker volume ls -q) 2>/dev/null
docker system prune -af --volumes
