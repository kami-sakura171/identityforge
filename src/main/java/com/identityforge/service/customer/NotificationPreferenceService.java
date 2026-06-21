package com.identityforge.service.customer;

import com.identityforge.model.NotificationPreference;
import com.identityforge.model.User;
import com.identityforge.repository.NotificationPreferenceRepository;
import com.identityforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Map<String, Boolean> getPreferences(Long userId) {
        Map<String, Boolean> prefs = new LinkedHashMap<>();
        for (String category : NotificationPreference.DEFAULT_CATEGORIES) {
            prefs.put(category, true);
        }
        preferenceRepository.findByUserId(userId)
                .forEach(p -> prefs.put(p.getCategory(), p.getEnabled()));
        return prefs;
    }

    @Transactional
    public Map<String, Boolean> updatePreferences(Long userId, Map<String, Boolean> preferences) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        for (Map.Entry<String, Boolean> entry : preferences.entrySet()) {
            // Find existing or create
            var existingPreferences = preferenceRepository.findByUserId(userId);
            NotificationPreference pref = existingPreferences.stream()
                    .filter(p -> p.getCategory().equals(entry.getKey()))
                    .findFirst()
                    .orElse(null);

            if (pref != null) {
                pref.setEnabled(entry.getValue());
                preferenceRepository.save(pref);
            } else {
                pref = NotificationPreference.builder()
                        .user(user)
                        .category(entry.getKey())
                        .enabled(entry.getValue())
                        .build();
                preferenceRepository.save(pref);
            }
        }

        return getPreferences(userId);
    }
}
