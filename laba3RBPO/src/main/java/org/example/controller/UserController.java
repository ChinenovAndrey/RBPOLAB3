package org.example.controller;

import org.example.dto.UserFullProfileDto;
import org.example.model.User;
import org.example.model.Supplier;
import org.example.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.repository.UserRepository;
import org.example.repository.SupplierRepository;
import org.example.repository.PurchaseRequestRepository;
import org.example.repository.CommercialOfferRepository;
import org.example.repository.PurchaseOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private PurchaseRequestRepository purchaseRequestRepository;
    @Autowired
    private CommercialOfferRepository commercialOfferRepository;
    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedData) {
        User updated = userService.updateUser(id, updatedData);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/full-profile")
    public ResponseEntity<UserFullProfileDto> getFullProfile(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        var requests = purchaseRequestRepository.findByAuthorId(id);

        var offers = commercialOfferRepository.findAll().stream()
                .filter(offer -> offer.getPurchaseRequest() != null &&
                        requests.contains(offer.getPurchaseRequest()))
                .toList();

        var orders = purchaseOrderRepository.findAll().stream()
                .filter(order -> order.getPurchaseRequest() != null &&
                        requests.contains(order.getPurchaseRequest()))
                .toList();

        var suppliers = commercialOfferRepository.findAll().stream()
                .filter(offer -> offer.getPurchaseRequest() != null &&
                        requests.contains(offer.getPurchaseRequest()))
                .map(offer -> (Supplier) offer.getSupplier())
                .distinct()
                .toList();

        UserFullProfileDto profile = new UserFullProfileDto(user, suppliers, requests, offers, orders);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/full-profiles/all")
    public ResponseEntity<List<UserFullProfileDto>> getAllFullProfiles() {
        List<User> users = userRepository.findAll();
        List<UserFullProfileDto> allProfiles = new ArrayList<>();

        for (User u : users) {
            Long currentId = u.getId();

            var requests = purchaseRequestRepository.findByAuthorId(currentId);

            var offers = commercialOfferRepository.findAll().stream()
                    .filter(offer -> offer.getPurchaseRequest() != null &&
                            requests.contains(offer.getPurchaseRequest()))
                    .toList();

            var orders = purchaseOrderRepository.findAll().stream()
                    .filter(order -> order.getPurchaseRequest() != null &&
                            requests.contains(order.getPurchaseRequest()))
                    .toList();

            var suppliers = commercialOfferRepository.findAll().stream()
                    .filter(offer -> offer.getPurchaseRequest() != null &&
                            requests.contains(offer.getPurchaseRequest()))
                    .map(offer -> (Supplier) offer.getSupplier())
                    .distinct()
                    .toList();

            allProfiles.add(new UserFullProfileDto(u, suppliers, requests, offers, orders));
        }

        return ResponseEntity.ok(allProfiles);
    }
}