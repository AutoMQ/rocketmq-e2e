{{- define "rocketmq-broker.config" -}}
{{- $name := include "rocketmq-broker.fullname" . }}
{{- $clusterName := include "rocketmq-broker.clusterName" . }}
{{- $brokerNamePrefix := include "rocketmq-broker.brokerNamePrefix" . }}
{{- $config := .Values.broker.config }}
{{- $s3stream := .Values.broker.s3stream }}
{{- $bindAddress := .Values.broker.service }}
{{- $db := .Values.broker.db }}
{{- $replicaCount := .Values.broker.replicaCount | int }}
{{- range $index := until $replicaCount }}
  {{ $name }}-{{ $clusterName }}: |
    name: {{ $clusterName }}-{{ $brokerNamePrefix }}
    instanceId: {{ $brokerNamePrefix }}-{{ $index }}
    bindAddress: "0.0.0.0:{{ $bindAddress.port }}"
    s3Stream:
      s3WALPath: {{ $s3stream.s3WALPath }}
      s3Endpoint: {{ $s3stream.s3Endpoint }}
      s3Bucket: {{ $s3stream.s3Bucket }}
      s3Region: {{ $s3stream.s3Region }}
      s3ForcePathStyle: {{ $s3stream.s3ForcePathStyle }}
      s3AccessKey: {{ $s3stream.s3AccessKey }}
      s3SecretKey: {{ $s3stream.s3SecretKey }}
    db:
      url: {{ $db.url }}
      userName: {{ $db.userName }}
      password: {{ $db.password }}
    metrics:
      exporterType: "OTLP_GRPC"
      grpcExporterTarget: "http://10.129.63.127:4317"
      grpcExporterHeader: ""
      grpcExporterTimeOutInMills: 31000
      periodicExporterIntervalInMills: 30000
      promExporterPort: 5557
      promExporterHost: "localhost"
      labels: ""
      exportInDelta: false
{{ $config | indent 4 }}
{{- end }}
{{- end }}