package au.org.aodn.nrmn.restapi.controller;

import au.org.aodn.nrmn.restapi.model.db.StagedJob;
import au.org.aodn.nrmn.restapi.model.db.StagedJobLog;
import au.org.aodn.nrmn.restapi.model.db.StagedRow;
import au.org.aodn.nrmn.restapi.model.db.StagedRowError;
import au.org.aodn.nrmn.restapi.model.db.audit.UserActionAudit;
import au.org.aodn.nrmn.restapi.model.db.enums.StagedJobEventType;
import au.org.aodn.nrmn.restapi.model.db.enums.StatusJobType;
import au.org.aodn.nrmn.restapi.repository.StagedJobLogRepository;
import au.org.aodn.nrmn.restapi.repository.StagedJobRepository;
import au.org.aodn.nrmn.restapi.repository.StagedRowRepository;
import au.org.aodn.nrmn.restapi.repository.UserActionAuditRepository;
import au.org.aodn.nrmn.restapi.service.SurveyIngestionService;
import au.org.aodn.nrmn.restapi.validation.process.RawValidation;
import cyclops.control.Validated;
import cyclops.data.Seq;
import cyclops.data.tuple.Tuple2;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/ingestion")
@Tag(name = "Ingestion")
public class IngestionController {
    @Autowired
    RawValidation validation;
    @Autowired
    SurveyIngestionService surveyIngestionService;
    @Autowired
    StagedJobLogRepository stagedJobLogRepository;
    @Autowired
    StagedJobRepository jobRepository;
    @Autowired
    StagedRowRepository rowRepository;
    @Autowired
    UserActionAuditRepository userActionAuditRepository;

    @GetMapping(path = "ingest/{job_id}")
    public ResponseEntity ingest(@PathVariable("job_id") Long jobId) {
        userActionAuditRepository.save(
                new UserActionAudit(
                        "ingestion/ingest",
                        "ingest job: " + jobId));

        Optional<StagedJob> optionalJob = jobRepository.findById(jobId);
        if (!optionalJob.isPresent()) {
            return ResponseEntity.badRequest().body("Job with given id does not exist. jobId: " + jobId);
        }

        StagedJob job = optionalJob.get();
        if(job.getStatus() != StatusJobType.PENDING) {
            return ResponseEntity.badRequest().body("Job with given id has not been validated: " + jobId);
        }

        job.setStatus(StatusJobType.INGESTING);
        jobRepository.save(job);

        try {
            stagedJobLogRepository.save(StagedJobLog.builder()
                    .stagedJob(job)
                    .eventType(StagedJobEventType.INGESTING)
                    .build());

            List<StagedRow> rows = rowRepository.findAll(Example.of(StagedRow.builder().stagedJob(job).build()));
            validation.preValidated(rows, job).forEach(surveyIngestionService::ingestStagedRow);

            job.setStatus(StatusJobType.INGESTED);
            jobRepository.save(job);

            stagedJobLogRepository.save(StagedJobLog.builder()
                    .stagedJob(job)
                    .eventType(StagedJobEventType.INGESTED)
                    .build());
        } catch (Exception e) {
            job.setStatus(StatusJobType.FAILED);
            jobRepository.save(job);
            stagedJobLogRepository.save(StagedJobLog.builder()
                    .stagedJob(job)
                    .details(e.getMessage())
                    .eventType(StagedJobEventType.ERROR)
                    .build());
            throw e;
        }

        return ResponseEntity.ok("job " + jobId + " sucessfully ingested.");
    }
}