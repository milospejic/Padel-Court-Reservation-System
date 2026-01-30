# Use the standard Fluentd image for Kubernetes and Elasticsearch as a base
FROM fluent/fluentd-kubernetes-daemonset:v1.4.2-debian-elasticsearch-1.1

# Install the plugin to detect and group multi-line Java stack traces
RUN gem install fluent-plugin-detect-exceptions -v 0.0.12 \
    && gem sources --clear-all \
    && rm -rf /var/lib/apt/lists/* \
    /home/fluent/.gem/ruby/2.3.0/cache/*.gem