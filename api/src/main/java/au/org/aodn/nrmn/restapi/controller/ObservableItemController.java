package au.org.aodn.nrmn.restapi.controller;

import au.org.aodn.nrmn.restapi.controller.exception.ResourceNotFoundException;
import au.org.aodn.nrmn.restapi.controller.exception.ValidationException;
import au.org.aodn.nrmn.restapi.controller.validation.ValidationError;
import au.org.aodn.nrmn.restapi.dto.observableitem.ObservableItemDto;
import au.org.aodn.nrmn.restapi.dto.observableitem.ObservableItemGetDto;
import au.org.aodn.nrmn.restapi.dto.observableitem.ObservableItemPutDto;
import au.org.aodn.nrmn.restapi.model.db.ObservableItem;
import au.org.aodn.nrmn.restapi.repository.ObservableItemRepository;
import au.org.aodn.nrmn.restapi.repository.projections.ObservableItemRow;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Tag(name = "observable items")
@RequestMapping(path = "/api/reference")
public class ObservableItemController {

    @Autowired
    private ObservableItemRepository observableItemRepository;

    @Autowired
    private ModelMapper mapper;
    
    @GetMapping(path = "/observableItems")
    public CollectionModel<ObservableItemRow> list() {
        return CollectionModel.of(
                observableItemRepository.findAllProjectedBy()
                .stream()
                .collect(Collectors.toList())
        );
    }

    @PostMapping("/observableItem")
    @ResponseStatus(HttpStatus.CREATED)
    public ObservableItemDto newObservableItem(@Valid @RequestBody ObservableItemDto observableItemDto) {
        ObservableItem newObservableItem = mapper.map(observableItemDto, ObservableItem.class);
        validate(newObservableItem);
        ObservableItem persistedObservableItem = observableItemRepository.save(newObservableItem);
        return mapper.map(persistedObservableItem, ObservableItemDto.class);
    }
    
    @GetMapping("/observableItem/{id}")
    public ResponseEntity<ObservableItemGetDto> findOne(@PathVariable Integer id) {
        return observableItemRepository.findById(id).map(item -> ResponseEntity.ok(mapper.map(item, ObservableItemGetDto.class))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/observableItem/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ObservableItemGetDto updateObservableItem(@PathVariable Integer id,
                                                     @Valid @RequestBody ObservableItemPutDto observableItemPutDto) {
        ObservableItem observableItem = observableItemRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        mapper.map(observableItemPutDto, observableItem);
        validate(observableItem);
        ObservableItem persistedObservableItem = observableItemRepository.save(observableItem);
        return mapper.map(persistedObservableItem, ObservableItemGetDto.class);
    }

    private void validate(ObservableItem item) {

        List<ValidationError> errors = new ArrayList<ValidationError>();

        ObservableItem probe = ObservableItem.builder().commonName(item.getCommonName()).letterCode(item.getLetterCode()).observableItemName(item.getObservableItemName()).build();
        Example<ObservableItem> example = Example.of(probe, ExampleMatcher.matchingAny());

        List<ObservableItem> allMatches = observableItemRepository.findAll(example);
        for(ObservableItem match: allMatches) {

            if(match.getObservableItemId().equals(item.getObservableItemId()))
                continue;

            if(match.getObservableItemName() != null && match.getObservableItemName().equals(item.getObservableItemName()))
                errors.add(new ValidationError(ObservableItemDto.class.getName(), "observableItemName", item.getObservableItemName(), "An item with this name already exists."));
            
            if(match.getLetterCode() != null && match.getLetterCode().equals(item.getLetterCode()))
                errors.add(new ValidationError(ObservableItemDto.class.getName(), "letterCode", item.getLetterCode(), "An item with this letter code already exists."));
        }

        if(!errors.isEmpty())
            throw new ValidationException(errors);
    }
}
