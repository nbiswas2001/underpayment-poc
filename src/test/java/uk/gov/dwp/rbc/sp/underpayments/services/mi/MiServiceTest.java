package uk.gov.dwp.rbc.sp.underpayments.services.mi;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MiServiceTest {

    @Autowired
    MiService miService;

    @Test
    void getOverview() {

       val ov = miService.getOverview();

    }

    @Test
    void testGetOverview() {
    }
}