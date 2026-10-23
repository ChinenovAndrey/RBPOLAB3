package org.example.controller;

import org.example.model.CommercialOffer;
import org.example.service.CommercialOfferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commercial-offers")
public class CommercialOfferController {

    private final CommercialOfferService offerService;

    public CommercialOfferController(CommercialOfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping
    public List<CommercialOffer> getAllOffers() {
        return offerService.getAllOffers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommercialOffer> getOfferById(@PathVariable Long id) {
        return offerService.getOfferById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CommercialOffer> createOffer(@RequestBody CommercialOffer offer) {
        CommercialOffer saved = offerService.createOffer(offer);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CommercialOffer> updateOffer(@PathVariable Long id, @RequestBody CommercialOffer updatedData) {
        CommercialOffer updated = offerService.updateOffer(id, updatedData);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        boolean deleted = offerService.deleteOffer(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}