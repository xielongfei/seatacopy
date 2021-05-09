package io.seata.metrics.exporter.prometheus;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.exporter.HTTPServer;
import io.seata.common.loader.LoadLevel;
import io.seata.config.ConfigurationFactory;
import io.seata.core.constants.ConfigurationKeys;
import io.seata.metrics.Measurement;
import io.seata.metrics.exporter.Exporter;
import io.seata.metrics.registry.Registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.seata.core.constants.ConfigurationKeys.METRICS_EXPORTER_PROMETHEUS_PORT;

/**
 * @author: xielongfei
 * @date: 2021/05/08 14:03
 * @description:
 */
@LoadLevel(name = "prometheus", order = 1)
public class PrometheusExporter extends Collector implements Collector.Describable, Exporter {

    private final HTTPServer server;

    private Registry registry;

    public PrometheusExporter() throws IOException {
        int port = ConfigurationFactory.getInstance().getInt(
                ConfigurationKeys.METRICS_PREFIX + METRICS_EXPORTER_PROMETHEUS_PORT, 9898);
        this.server = new HTTPServer(port, true);
        this.register();
    }

    @Override
    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> familySamples = new ArrayList<>();
        if (registry != null) {
            Iterable<Measurement> measurements = registry.measure();
            List<MetricFamilySamples.Sample> samples = new ArrayList<>();
            measurements.forEach(measurement -> samples.add(convertMeasurementToSample(measurement)));

            if (!samples.isEmpty()) {
                familySamples.add(new MetricFamilySamples("seata", Type.UNTYPED, "seata", samples));
            }
        }
        return familySamples;
    }

    private Sample convertMeasurementToSample(Measurement measurement) {
        String prometheusName = measurement.getId().getName().replace(".", "_");
        List<String> labelNames = new ArrayList<>();
        List<String> labelValues = new ArrayList<>();
        for (Map.Entry<String, String> tag : measurement.getId().getTags()) {
            labelNames.add(tag.getKey());
            labelValues.add(tag.getValue());
        }
        return new Sample(prometheusName, labelNames, labelValues, measurement.getValue(),
                (long)measurement.getTimestamp());
    }

    @Override
    public List<MetricFamilySamples> describe() {
        return collect();
    }

    @Override
    public void close() {
        server.stop();
    }
}
