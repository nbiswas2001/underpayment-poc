package uk.gov.dwp.rbc.sp.underpayments.endpoints;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import uk.gov.dwp.rbc.sp.underpayments.services.narrator.NarratorService;
import uk.gov.dwp.rbc.sp.underpayments.utils.UeException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.rmi.UnexpectedException;

@CrossOrigin
@RestController
public class NarratorEP {

    @Autowired
    NarratorService narratorService;

    //----------------------------------
    @GetMapping("/narrate/{schema}/{nino}")
    public ResponseEntity<StreamingResponseBody> getNarration(@PathVariable String schema, @PathVariable String nino) {
        StreamingResponseBody responseBody = response -> {
            String narr = null;
            try {
                narr = narratorService.getNarration(schema, nino);
            } catch (Exception e) {
                val w = new StringWriter();
                e.printStackTrace(new PrintWriter(w));
                narr = "<htm><body style=\"font-family:monospace;\"><h2>Error getting narration</h2>"
                        + "<h4>Cause: " + e.getClass().getSimpleName()
                        + ", Message: " + e.getMessage() + "</h4>"
                        + "<p>" + w.toString().replaceAll("\n", "<br>") +"</p>"
                        +"</body></html>";
                e.printStackTrace();
            }
            response.write(narr.getBytes(StandardCharsets.UTF_8));
        };

        val hdrs = new HttpHeaders();
        hdrs.add("content-disposition",
                "attachment;filename=narration_"+nino+".html");
        hdrs.add("Content-Type", MediaType.TEXT_HTML_VALUE);

        return ResponseEntity
                .ok()
                .headers(hdrs)
                .body(responseBody);
    }
}
