@REM kubectl apply -f ./server/nginx-ingress.yaml
@REM TIMEOUT /t 20 /nobreak > NUL
@REM kubectl apply -f ./server/netty-deployment.yaml
@REM TIMEOUT /t 5 /nobreak > NUL
@REM minikube tunnel

PAUSE