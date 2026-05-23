package com.example.pokerclub.report;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController //принимает http-запросы и возвращает ответ клиенту
// но здесь уже будет возвращаться не dto а файл
@RequestMapping("/api/admin/tournaments")
public class ReportController {

    private final ReportService reportService; //подключаем сервис для формирования отчета


    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    //метод для обраотки запроса  GET /api/admin/tournaments/1/reports.csv
    @GetMapping("/{tournamentId}/reports.csv")
    public ResponseEntity<byte[]> downloadCsvReport(@PathVariable Long tournamentId) {
        byte[] report = reportService.generateTournamentCsvReport(tournamentId); //формируется файл CSV сервисом
        //переменая reports содержит байты файла

        //настраивает Http-ответ
        return ResponseEntity.ok()
                //CONTENT_DISPOSITION говорит браузеру - это файл, его нужно скачать как вложение.
                //имя файла: tournament-1-report.csv
                .header(HttpHeaders.CONTENT_DISPOSITION, attachment("tournament-" + tournamentId + "-report.csv"))
                .contentType(new MediaType("text", "csv")) //тип содержимого
                .body(report); //передача байтов файла как http-ответа
    }

    //такой же метод как и у CSV но для PDF
    @GetMapping("/{tournamentId}/reports.pdf")
    public ResponseEntity<byte[]> downloadPdfReport(@PathVariable Long tournamentId) {
        byte[] report = reportService.generateTournamentPdfReport(tournamentId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, attachment("tournament-" + tournamentId + "-report.pdf"))
                .contentType(MediaType.APPLICATION_PDF)
                .body(report);
    }

    //формирует значение http-заголовка
    //без него браузер просто откроет файл или покажет набор символов
    //а с этим заголовком он понимает что фалй нужно скачать
    private String attachment(String filename) {
        return ContentDisposition.attachment()
                .filename(filename)
                .build()
                .toString();
    }
}