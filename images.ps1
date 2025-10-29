Write-Host "Desplegando con Helm..."

helm upgrade --install devops-tools ./helm-charts/devops --namespace devops --create-namespace
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error desplegando con Helm"
    exit 1
}

Write-Host "Despliegue completado."
