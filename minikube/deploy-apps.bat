@REM Env Vars
kubectl apply -f env-configmap.yaml
@REM Secrets
kubectl apply -f secrets-deployment.yaml

@REM @REM Apps
@REM kubectl apply -f ./apps/search-app-deployment.yaml
@REM kubectl apply -f ./apps/recommendation-app-deployment.yaml
@REM kubectl apply -f ./apps/thread-app-deployment.yaml
@REM kubectl apply -f ./apps/subthread-app-deployment.yaml
@REM kubectl apply -f ./apps/user-to-user-actions-app-deployment.yaml

PAUSE