TIMEOUT := 1200s
INGRESS_CONTROLLER_VERSION := 4.10.0

ingress-controller:
    kubectl create clusterrolebinding cluster-admin-binding --clusterrole cluster-admin --user $(gcloud config get-value account) &&
    kubectl apply -f ingress-controller/tcp-services-configmap.yaml &&
    kubectl apply -f ingress-controller/ingress-nginx.yaml

.PHONY: ingress-controller