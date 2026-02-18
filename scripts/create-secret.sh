#!/bin/bash
# Script pour créer le secret dans AWS Secrets Manager
# Usage: ./create-secret.sh <RDS_ENDPOINT> <DB_PASSWORD>

set -e

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <RDS_ENDPOINT> <DB_PASSWORD>"
    echo "Exemple: $0 topmatter-db.xxxxx.us-east-1.rds.amazonaws.com MonMotDePasse123!"
    exit 1
fi

RDS_ENDPOINT=$1
DB_PASSWORD=$2
SECRET_NAME="topmatter-db-credentials"
REGION="us-east-1"

# Créer le JSON du secret
SECRET_JSON=$(cat <<EOF
{
  "username": "postgres",
  "password": "${DB_PASSWORD}",
  "engine": "postgres",
  "host": "${RDS_ENDPOINT}",
  "port": 5432,
  "dbname": "social_network"
}
EOF
)

# Vérifier si le secret existe déjà
if aws secretsmanager describe-secret --secret-id "$SECRET_NAME" --region "$REGION" 2>/dev/null; then
    echo "Le secret existe déjà. Mise à jour..."
    aws secretsmanager update-secret \
        --secret-id "$SECRET_NAME" \
        --secret-string "$SECRET_JSON" \
        --region "$REGION"
    echo "Secret mis à jour avec succès!"
else
    echo "Création du secret..."
    aws secretsmanager create-secret \
        --name "$SECRET_NAME" \
        --description "Credentials for Topmatter PostgreSQL database" \
        --secret-string "$SECRET_JSON" \
        --region "$REGION"
    echo "Secret créé avec succès!"
fi

# Afficher le secret (pour vérification)
echo ""
echo "Vérification du secret:"
aws secretsmanager get-secret-value \
    --secret-id "$SECRET_NAME" \
    --region "$REGION" \
    --query SecretString \
    --output text | jq .
