package au.org.aodn.nrmn.restapi.service;

import au.org.aodn.nrmn.restapi.service.model.SpeciesRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WormsService {

    private final WebClient wormsClient;

    @Autowired
    public WormsService(@Qualifier("wormsClient") WebClient wormsClient) {
        this.wormsClient = wormsClient;
    }

    public List<SpeciesRecord> fuzzyNameSearch(String searchTerm) {
        Mono<SpeciesRecord[][]> response = wormsClient
                .get().uri(uriBuilder ->
                        uriBuilder.path("/AphiaRecordsByMatchNames")
                                  .queryParam("scientificnames[]", searchTerm)
                                  .build())
                .retrieve()
                .bodyToMono(SpeciesRecord[][].class);
        SpeciesRecord[][] matchingSpecies = Optional.ofNullable(response.block())
                                                   .orElse(new SpeciesRecord[0][0]);
        return Arrays.stream(matchingSpecies)
                     .flatMap(children -> Arrays.stream(children))
                     .collect(Collectors.toList());
    }
}
