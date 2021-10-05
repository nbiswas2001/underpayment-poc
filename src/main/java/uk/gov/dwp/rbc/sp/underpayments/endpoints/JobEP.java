package uk.gov.dwp.rbc.sp.underpayments.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.dwp.rbc.sp.underpayments.services.job.JobService;

@CrossOrigin
@RestController
public class JobEP {

    @Autowired
    private JobService jobService;

    //----------------------------------
    @GetMapping("/jobs/run/{jobName}")
    public ResponseEntity<JobStartResult> runJob(@PathVariable String jobName) {
        try {
            jobService.runJob(jobName);
            return new ResponseEntity<>(new JobStartResult("OK"), HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(new JobStartResult(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //-------------------------------
    public class JobStartResult {
        public String message;

        public JobStartResult(String message) {
            this.message = message;
        }
    }
}
