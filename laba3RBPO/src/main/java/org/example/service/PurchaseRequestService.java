package org.example.service;

import org.example.exception.ApiException;
import org.example.model.*;
import org.example.repository.CommercialOfferRepository;
import org.example.repository.PurchaseRequestRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PurchaseRequestService {

    private final PurchaseRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final CommercialOfferRepository offerRepository;

    public PurchaseRequestService(PurchaseRequestRepository requestRepository,
                                  UserRepository userRepository,
                                  CommercialOfferRepository offerRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.offerRepository = offerRepository;
    }

    public List<PurchaseRequest> getAllRequests() {
        return requestRepository.findAll();
    }

    public Optional<PurchaseRequest> getRequestById(Long id) {
        return requestRepository.findById(id);
    }

    public PurchaseRequest createRequest(PurchaseRequest request) {
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw ApiException.badRequest("Описание заявки обязательно");
        }
        if (request.getMaxBudget() == null || request.getMaxBudget() <= 0) {
            throw ApiException.badRequest("Максимальный бюджет должен быть больше нуля");
        }
        if (request.getAuthor() == null || request.getAuthor().getId() == null) {
            throw ApiException.badRequest("Укажите автора заявки, например: \"author\": {\"id\": 1}");
        }
        User author = userRepository.findById(request.getAuthor().getId())
                .orElseThrow(() -> ApiException.badRequest("Автор с таким id не найден"));

        request.setId(null);
        request.setAuthor(author);
        request.setStatus(RequestStatus.OPEN);
        request.setCreatedAt(LocalDateTime.now());
        return requestRepository.save(request);
    }

    @Transactional
    public PurchaseRequest updateRequest(Long id, PurchaseRequest updatedData) {
        PurchaseRequest request = requestRepository.findById(id).orElse(null);
        if (request == null) {
            return null;
        }
        if (request.getStatus() != RequestStatus.OPEN) {
            throw ApiException.conflict("Изменять можно только открытую заявку");
        }
        if (updatedData.getDescription() != null) {
            if (updatedData.getDescription().isBlank()) {
                throw ApiException.badRequest("Описание не может быть пустым");
            }
            request.setDescription(updatedData.getDescription());
        }
        if (updatedData.getMaxBudget() != null) {
            double newBudget = updatedData.getMaxBudget();
            if (newBudget <= 0) {
                throw ApiException.badRequest("Максимальный бюджет должен быть больше нуля");
            }
            boolean tooLow = offerRepository.findByPurchaseRequestId(id).stream()
                    .anyMatch(o -> o.getStatus() == OfferStatus.NEW && o.getPrice() > newBudget);
            if (tooLow) {
                throw ApiException.conflict("Есть новые предложения дороже нового бюджета");
            }
            request.setMaxBudget(newBudget);
        }
        return request;
    }

    public boolean deleteRequest(Long id) {
        if (requestRepository.existsById(id)) {
            requestRepository.deleteById(id);
            return true;
        }
        return false;
    }
}