{{- define "rocketmq-broker.config" -}}
{{- $name := include "rocketmq-broker.fullname" . }}
{{- $clusterName := include "rocketmq-broker.clusterName" . }}
{{- $brokerNamePrefix := include "rocketmq-broker.brokerNamePrefix" . }}
{{- $config := .Values.broker.config }}
{{- $s3stream := .Values.broker.s3stream }}
{{- $db := .Values.broker.db }}
{{- $replicaCount := .Values.broker.replicaCount | int }}
{{- range $index := until $replicaCount }}
  {{ $name }}-{{ $index }}: |
    name: {{ $clusterName }}-{{ $brokerNamePrefix }}
    instanceId: {{ $brokerNamePrefix }}-{{ $index }}
    bindAddress: "0.0.0.0:8081"
    s3Stream:
      s3WALPath: {{ $s3stream.s3WALPath }}
      s3Endpoint: {{ $s3stream.s3Endpoint }}
      s3Bucket: {{ $s3stream.s3Bucket }}
      s3Region: {{ $s3stream.s3Region }}
      s3ForcePathStyle: {{ $s3stream.s3ForcePathStyle }}
      s3AccessKey: {{ $s3stream.s3AccessKey }}
      s3SecretKey: {{ $s3stream.s3SecretKey }}
    db:
      url: {{ $db.dbURL }}
      userName: {{ $db.dbUserName }}
      password: {{ $db.dbPassword }}
    metrics:
      exporterType: "PROM"
      grpcExporterTarget: ""
      grpcExporterHeader: ""
      grpcExporterTimeOutInMills: 31000
      grpcExporterIntervalInMills: 60000
      promExporterPort: 5557
      promExporterHost: "localhost"
      loggingExporterIntervalInMills: 10000
      labels: ""
      exportInDelta: false
{{ $config | indent 4 }}
{{- end }}
{{- end }}