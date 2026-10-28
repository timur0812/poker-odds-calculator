package io.github.timur0812.pokerodds.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/api/analyze")
    public AnalyzeResponse analyze(@RequestBody AnalyzeRequest request) {
        return analysisService.analyze(request);
    }
}
