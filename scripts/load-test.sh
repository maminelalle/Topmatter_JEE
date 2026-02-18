#!/bin/bash
# Script de test de charge pour l'application Topmatter
# Usage: ./load-test.sh <ALB_URL>

set -e

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <ALB_URL>"
    echo "Exemple: $0 http://topmatter-alb-xxxxx.us-east-1.elb.amazonaws.com"
    exit 1
fi

ALB_URL=$1
REGION="us-east-1"
ASG_NAME="topmatter-asg"

echo "=========================================="
echo "Test de charge pour Topmatter"
echo "URL: $ALB_URL"
echo "=========================================="
echo ""

# Vérifier le nombre d'instances avant le test
echo "📊 État initial de l'Auto Scaling Group:"
aws autoscaling describe-auto-scaling-groups \
    --auto-scaling-group-names "$ASG_NAME" \
    --region "$REGION" \
    --query 'AutoScalingGroups[0].[DesiredCapacity,MinSize,MaxSize]' \
    --output table

echo ""
echo "Instances actuelles:"
aws autoscaling describe-auto-scaling-groups \
    --auto-scaling-group-names "$ASG_NAME" \
    --region "$REGION" \
    --query 'AutoScalingGroups[0].Instances[*].[InstanceId,LifecycleState,HealthStatus]' \
    --output table

echo ""
echo "🚀 Démarrage du test de charge..."
echo "1000 requêtes avec 50 connexions simultanées"
echo ""

# Test avec Apache Bench (si disponible)
if command -v ab &> /dev/null; then
    ab -n 1000 -c 50 -v 2 "$ALB_URL/" | tee /tmp/loadtest-results.txt
    
    echo ""
    echo "✅ Test terminé. Résultats sauvegardés dans /tmp/loadtest-results.txt"
else
    echo "⚠️  Apache Bench (ab) n'est pas installé."
    echo "Installation: sudo apt-get install apache2-utils"
    echo ""
    echo "Test alternatif avec curl (100 requêtes):"
    for i in {1..100}; do
        curl -s -o /dev/null -w "Requête $i: %{http_code}\n" "$ALB_URL/"
        if [ $((i % 10)) -eq 0 ]; then
            echo "--- $i requêtes effectuées ---"
        fi
    done
fi

echo ""
echo "⏳ Attente de 2 minutes pour observer le scaling..."
sleep 120

echo ""
echo "📊 État après le test:"
aws autoscaling describe-auto-scaling-groups \
    --auto-scaling-group-names "$ASG_NAME" \
    --region "$REGION" \
    --query 'AutoScalingGroups[0].Instances[*].[InstanceId,LifecycleState,HealthStatus]' \
    --output table

echo ""
echo "📈 Historique des activités Auto Scaling:"
aws autoscaling describe-scaling-activities \
    --auto-scaling-group-name "$ASG_NAME" \
    --region "$REGION" \
    --max-items 10 \
    --query 'Activities[*].[StartTime,StatusCode,Description]' \
    --output table

echo ""
echo "✅ Test terminé!"
