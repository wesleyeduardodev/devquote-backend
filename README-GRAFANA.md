# Monitoramento com Grafana + Prometheus

## 🚀 Subir os serviços

```bash
wsl bash -c "cd devquote-backend && docker compose -f docker-compose-monitoring.yml up -d"
```

## 🎯 Configurar Grafana

1. Acesse: http://localhost:3001
   - **Usuário:** `admin`
   - **Senha:** `admin`

2. Adicionar Data Source:
   - **Configuration** → **Data Sources** → **Add data source**
   - Selecione **Prometheus**
   - **URL:** `http://prometheus:9090`
   - **Save & Test**

3. Importar Dashboard:
   - **Dashboards** → **Import**
   - Digite o ID: **11378**
   - Selecione **Prometheus** como Data Source
   - **Import**

## 📊 Dashboards Recomendados

| ID | Nome |
|----|------|
| 11378 | Spring Boot 2.1 System Monitor |
| 4701 | JVM (Micrometer) |
| 12900 | Spring Boot 2.x Statistics |

## 🔗 URLs

- **Grafana:** http://localhost:3001
- **Prometheus:** http://localhost:9090
- **Backend:** http://localhost:8080
- **Métricas:** http://localhost:8080/actuator/prometheus

## 🛑 Parar os serviços

```bash
wsl bash -c "cd devquote-backend && docker compose -f docker-compose-monitoring.yml down"
```
