package au.org.aodn.nrmn.restapi.controller;

import au.org.aodn.nrmn.restapi.dto.payload.ErrorInput;
import au.org.aodn.nrmn.restapi.dto.stage.FileUpload;
import au.org.aodn.nrmn.restapi.dto.stage.UploadResponse;
import au.org.aodn.nrmn.restapi.dto.stage.ValidationResponse;
import au.org.aodn.nrmn.restapi.model.db.StagedJob;
import au.org.aodn.nrmn.restapi.model.db.StagedRow;
import au.org.aodn.nrmn.restapi.model.db.audit.UserActionAudit;
import au.org.aodn.nrmn.restapi.model.db.enums.SourceJobType;
import au.org.aodn.nrmn.restapi.model.db.enums.StatusJobType;
import au.org.aodn.nrmn.restapi.repository.*;
import au.org.aodn.nrmn.restapi.service.SpreadSheetService;
import au.org.aodn.nrmn.restapi.service.StagedRowService;
import au.org.aodn.nrmn.restapi.util.ValidatorHelpers;
import au.org.aodn.nrmn.restapi.validation.process.ValidationProcess;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(path = "/api/stage")
@Tag(name = "staged jobs")
@CrossOrigin
public class StagedJobController {

    @Autowired
    SpreadSheetService sheetService;

    @Autowired
    StagedRowService rowService;

    @Autowired

    StagedRowRepository stagedRowRepo;

    @Autowired
    UserActionAuditRepository userAuditRepo;

    @Autowired
    ProgramRepository programRepo;

    @Autowired
    private StagedJobRepository jobRepo;

    @Autowired
    private ValidationProcess validation;

    @Autowired
    private SecUserRepository userRepo;


    @PostMapping("/upload")
    @Operation(security = {@SecurityRequirement(name = "bearer-key")})
    public ResponseEntity<UploadResponse> uploadFile(
            @RequestParam("withInvertSize") Boolean withInvertSize,
            @RequestParam("programId") Integer programId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        userAuditRepo.save(
                new UserActionAudit(
                        "stage/upload",
                        "upload excel file attempt for username: " + authentication.getName()
                                + "file: " + file.getOriginalFilename())
        );
        val programOpt = programRepo.findById(programId);
        if (!programOpt.isPresent())
            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new UploadResponse(Optional.empty(),
                            Stream.of(new ErrorInput("Program Not found", "program")).collect(Collectors.toList())));
        val user = userRepo.findByEmail(authentication.getName());

        val job = StagedJob.builder()
                .isExtendedSize(withInvertSize)
                .source(SourceJobType.FILE)
                .reference(file.getOriginalFilename())
                .status(StatusJobType.PENDING)
                .program(programOpt.get())
                .creator(user.get())
                .build();
       val persitedJob = jobRepo.save(job);

        val validationHelper = new ValidatorHelpers();
        val validatedSheet =
                sheetService
                        .validatedExcelFile(
                                file.getOriginalFilename() + "-" + persitedJob.getId(),
                                file,
                                withInvertSize);



        List<ErrorInput> errors = validationHelper.toErrorList(validatedSheet);

        return validatedSheet.fold(
                err -> {
                    job.setStatus(StatusJobType.FAILED);
                    jobRepo.save(job);
                  return  ResponseEntity.unprocessableEntity().
                            body(new UploadResponse(Optional.empty(), errors));
                },
                sheet -> {
                    val stagedRowToSave = sheetService.sheets2Staged(sheet);
                    job.setStatus(StatusJobType.STAGED);
                    jobRepo.save(job);
                    stagedRowRepo.saveAll(stagedRowToSave.stream().map(s -> {
                        s.setStagedJob(job);
                        return s;
                    })
                            .collect(Collectors.toList()));
                    val filesResult = new FileUpload(job.getId(), stagedRowToSave.size());
                    return ResponseEntity
                            .status(HttpStatus.OK)
                            .body(new UploadResponse(Optional.of(filesResult), Collections.emptyList()));
                });
    }

    @PutMapping("/update/{rowId}")
    @Operation(security = {@SecurityRequirement(name = "bearer-key")})
    public ResponseEntity updateRow(@PathVariable Long rowId,
                                    Authentication authentication,
                                    @RequestBody StagedRow newRow) {
        return rowService.update(rowId, newRow).fold(err ->
                        ResponseEntity
                                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                                .body(err)
                , rowUpdate -> ResponseEntity.ok().body(rowUpdate));
    }


    @PostMapping("/validate/{jobId}")
    @Operation(security = {@SecurityRequirement(name = "bearer-key")})
    public ResponseEntity validateJob(
            @PathVariable Long jobId,
            Authentication authentication) {

        userAuditRepo.save(
                new UserActionAudit(
                        "stage/validate",
                        "validate job attempt for username " + authentication.getName()
                                + " file: " + jobId)
        );

        return jobRepo.findById(jobId).map(job -> {
            val validationResponse = validation.process(job);
            return ResponseEntity.ok().body(validationResponse);
        }).orElseGet(() -> ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ValidationResponse(
                        null,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.singletonList(new ErrorInput("StagedJob Not found", "StagedJob")))));
    }

    @GetMapping("/job/{jobId}")
    @Operation(security = {@SecurityRequirement(name = "bearer-key")})
    public ValidationResponse getJob(@PathVariable  Long jobId) {
        val rows = stagedRowRepo.findRowsByJobId(jobId);
        return jobRepo.findById(jobId)
                .map(job ->
                        new ValidationResponse(
                                job, rows, Collections.emptyList(), Collections.emptyList()
                        )
                ).orElseGet(() ->
                        new ValidationResponse(
                                null,
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.singletonList(new ErrorInput("StagedJob Not found", "StagedJob"))
                        ));

    }
}

