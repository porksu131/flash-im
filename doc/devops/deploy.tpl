apiVersion: apps/v1
kind: Deployment
metadata:
  namespace: dev
  name: ${app_name}
  labels:
    app: ${app_name}
spec:
  replicas: ${replicas}
  selector:
    matchLabels:
      app: ${app_name}
  strategy:
    rollingUpdate:
      maxSurge: 50%
      maxUnavailable: 50%
    type: RollingUpdate
  template:
    metadata:
      labels:
        app: ${app_name}
    spec:
      containers:
        - name: ${app_name}
          image: ${image_name}
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: ${container_port}

---
apiVersion: v1
kind: Service
metadata:
  namespace: dev
  labels:
    app: ${app_name}
  name: ${app_name}
spec:
  selector:
    app: ${app_name}
  ports:
    - port: ${service_port}
      targetPort: ${container_port}
  type: ClusterIP

###INGRESS_START###
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  namespace: dev
  name: ${app_name}
spec:
  ingressClassName: ingress
  rules:
    - host: ${app_name}.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: ${app_name}
                port:
                  number: ${service_port}
###INGRESS_END###