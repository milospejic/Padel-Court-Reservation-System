package reservation_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "padel-exchange";
    public static final String CLEANUP_QUEUE = "reservation-cleanup-queue";

    // Keep existing notification queue
    @Bean
    public Queue notificationQueue() {
        return new Queue("notification-queue", true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue cleanupQueue() {
        return new Queue(CLEANUP_QUEUE, true);
    }

    @Bean
    public Binding binding(Queue cleanupQueue, TopicExchange exchange) {
        return BindingBuilder.bind(cleanupQueue).to(exchange).with("club.deleted");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}