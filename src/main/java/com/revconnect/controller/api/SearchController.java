// src/main/java/com/revconnect/controller/api/SearchController.java
package com.revconnect.controller.api;

import com.revconnect.dto.request.UserSearchRequest;
import com.revconnect.dto.response.EnhancedProfileResponse;
import com.revconnect.dto.response.PageResponse;
import com.revconnect.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/live")
    public ResponseEntity<?> liveSearch(@RequestParam String query) {
        try {
            UserSearchRequest request = new UserSearchRequest();
            request.setQuery(query);
            request.setPage(0);
            request.setSize(5); // Return top 5 results for live search

            PageResponse<EnhancedProfileResponse> results = searchService.searchVisibleUsers(request);
            return ResponseEntity.ok(results.getContent());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error performing live search");
        }
    }

    @GetMapping
    public ResponseEntity<?> searchUsers(
            @RequestParam String q,
            @RequestParam(required = false) String userType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        try {
            UserSearchRequest request = new UserSearchRequest();
            request.setQuery(q);
            request.setUserType(userType);
            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sortBy);
            request.setSortDirection(sortDirection);

            PageResponse<EnhancedProfileResponse> results = searchService.searchVisibleUsers(request);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error performing search");
        }
    }
}