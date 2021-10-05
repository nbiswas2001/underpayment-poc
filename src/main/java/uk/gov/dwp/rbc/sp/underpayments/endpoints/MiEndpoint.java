package uk.gov.dwp.rbc.sp.underpayments.endpoints;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import uk.gov.dwp.rbc.sp.underpayments.repositories.AccountRepo;
import uk.gov.dwp.rbc.sp.underpayments.services.mi.MiService;
import uk.gov.dwp.rbc.sp.underpayments.services.mi.OverviewRec;

@CrossOrigin
@RestController
public class MiEndpoint {

    @Autowired
    MiService miService;

    @Autowired
    AccountRepo accountRepo;

    //----------------------------------
    @GetMapping("/mi/overview")
    public OverviewRec getOverview() {
        return miService.getOverview();
    }

    //----------------------------------
    @GetMapping("/mi/reports/flags")
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> getReportingFlags(@RequestParam Long fromId, @RequestParam Long toId) {

        StreamingResponseBody responseBody = response -> {

//            val sb1 = new StringBuilder();
//            sb1.append(ReportingFlags.getHeaderRow(ReportingFlags.Category.values()))
//                    .append(",")
//                    .append(ErrorFlags.getHeaderRow(ErrorFlags.Error.values()))
//                    .append("\n");
//
//            response.write(sb1.toString().getBytes(StandardCharsets.UTF_8));

            //TODO XXX
//            for(val rec : accountRepo.getReportingFlags(fromId, toId)){
//                val sb2 = new StringBuilder();
//                sb2.append(new ReportingFlags(rec.reportingFlags).getDataRow())
//                        .append(",")
//                        .append(new ErrorFlags(rec.errorFlags).getDataRow())
//                        .append("\n");
//                response.write(sb2.toString().getBytes(StandardCharsets.UTF_8));
//            }
        };

        val hdrs = new HttpHeaders();
        hdrs.add("content-disposition",
                "attachment;filename=reporting_flags_"+fromId+"-"+toId+".csv");
        hdrs.add("Content-Type",MediaType.TEXT_PLAIN_VALUE);

        return ResponseEntity
                .ok()
                .headers(hdrs)
                .body(responseBody);
    }
}
