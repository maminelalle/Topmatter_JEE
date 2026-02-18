#!/bin/bash
# Script User Data pour les instances EC2 dans Auto Scaling Group
# Ce script sera exécuté au démarrage de chaque nouvelle instance

set -e

# Variables (à adapter selon votre configuration)
REPO_URL="https://github.com/VOTRE_USERNAME/Topmatter.git"  # Remplacez par votre repo
BRANCH="main"  # ou "master"

# Mettre à jour le système
apt-get update
apt-get upgrade -y

# Installer les dépendances
apt-get install -y \
    curl \
    git \
    docker.io \
    docker-compose \
    postgresql-client

# Démarrer Docker
systemctl start docker
systemctl enable docker
usermod -aG docker ubuntu

# Attendre que Docker soit prêt
sleep 10

# Cloner ou mettre à jour le projet
cd /home/ubuntu
if [ -d "Topmatter" ]; then
    cd Topmatter
    git pull origin "$BRANCH"
else
    git clone -b "$BRANCH" "$REPO_URL" Topmatter
    cd Topmatter
fi

# Attendre que le réseau soit prêt
sleep 5

# Lancer l'application avec Docker Compose
docker-compose -f docker-compose.aws.yml up -d --build

# Logs pour debugging
echo "Application démarrée à $(date)" >> /var/log/topmatter-startup.log
docker-compose -f docker-compose.aws.yml ps >> /var/log/topmatter-startup.log
