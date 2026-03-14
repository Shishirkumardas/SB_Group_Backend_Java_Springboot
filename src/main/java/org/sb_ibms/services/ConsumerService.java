package org.sb_ibms.services;

import lombok.AllArgsConstructor;
import org.sb_ibms.ResourceNotFoundException;
import org.sb_ibms.models.Consumer;
import org.sb_ibms.repositories.ConsumerRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ConsumerService {
    private ConsumerRepository consumerRepository;

    public Consumer updateConsumer(Long id, Consumer consumerDetails) {
        Consumer consumer = consumerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consumer not found"));
        consumer.setName(consumerDetails.getName());
        consumer.setArea(consumerDetails.getArea());
        consumer.setPurchaseAmount(consumerDetails.getPurchaseAmount());
        consumer.setDueAmount(consumerDetails.getDueAmount());
        return consumerRepository.save(consumer);
    }

}
