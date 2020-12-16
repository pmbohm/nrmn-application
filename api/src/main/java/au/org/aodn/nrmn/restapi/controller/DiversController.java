package au.org.aodn.nrmn.restapi.controller;

import au.org.aodn.nrmn.restapi.service.CsvService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "/api/divers")
@Tag(name = "divers")
public class DiversController {
    @Autowired
    private CsvService csvService;

    @GetMapping("/divers.csv")
    public void getCsv(final HttpServletResponse response) throws IOException {
        response.setContentType("application/csv");
        csvService.getDiversCsv(response.getWriter());
    }


    @GetMapping("/sites.csv")
    public void getSitesCsv(final HttpServletResponse response,
                            @RequestParam (required = false) List<Integer> locations,
                            @RequestParam (required = false) List<String> provinces,
                            @RequestParam (required = false) List<String> states,
                            @RequestParam (required = false) List<String> siteCodes) throws IOException {
        response.setContentType("application/csv");
        csvService.getSitesCsv(response.getWriter(), locations, provinces, states, siteCodes);
    }
}
