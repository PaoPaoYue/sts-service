TIMEOUT := 1200s
DATADOG_VERSION := 3.61.0
INGRESS_CONTROLLER_VERSION := 4.10.0
YPP_RPC_MESH_PROXY_VERSION := 0.0.8
YPP_RPC_DEMO_VERSION := 0.0.6
REDIS_VERSION := 20.1.4

# remember to set the data-dog api key in datadog-secret before running this
# > helm repo add datadog https://helm.datadoghq.com
# > kubectl create secret generic datadog-secret --from-literal api-key=<DATADOG_API_KEY> --namespace datadog
datadog:
	helm upgrade --wait --timeout=$(TIMEOUT) --version ${DATADOG_VERSION} --create-namespace --namespace datadog --install --values datadog/values.yaml datadog-agent datadog/datadog

ingress-controller:
	kubectl create clusterrolebinding cluster-admin-binding --clusterrole cluster-admin --user $(gcloud config get-value account)
	kubectl apply -f ingress-controller/tcp-services-configmap.yaml --namespace=ingress-nginx
	kubectl apply -f ingress-controller/ingress-nginx.yaml --namespace=ingress-nginx

# remember to add helm repo
# > helm repo add ypp-rpc https://paopaoyue.github.io/mesh
mesh-proxy:
	helm upgrade --wait --timeout=$(TIMEOUT) --version ${YPP_RPC_MESH_PROXY_VERSION} --create-namespace --namespace default --install mesh-proxy ypp-rpc/mesh-proxy

demo:
	helm upgrade --wait --timeout=$(TIMEOUT) --version ${YPP_RPC_DEMO_VERSION} --create-namespace --namespace default --install demo ypp-rpc/demo

# remember to set the clickhouse password in clickhouse-passwd-secret before running this
# > kubectl create secret generic clickhouse-passwd-secret --from-literal test=<PASSWORD> --from-literal prod=<PASSWORD> --from-literal admin=<PASSWORD> --from-literal readonly=<PASSWORD> --namespace default
clickhouse:
	kubectl apply -f https://raw.githubusercontent.com/Altinity/clickhouse-operator/master/deploy/operator/clickhouse-operator-install-bundle.yaml
	kubectl apply -f clickhouse/clickhouse-cluster.yaml

# remember to add helm repo
# > helm repo add bitnami https://charts.bitnami.com/bitnami
# remember to set the redis password in redis-passwd-secret before running this
# > kubectl create secret generic redis-passwd-secret --from-literal redis-password=<REDIS_PASSWORD> --namespace default
redis:
	helm upgrade --wait --timeout=$(TIMEOUT) --version ${REDIS_VERSION} --create-namespace --namespace default --install --values redis/values.yaml  redis bitnami/redis

.PHONY: datadog ingress-controller mesh-proxy demo redis clickhouse