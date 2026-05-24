package com.example.pokerclub.report;

//собирает данные и возвращает файл в виде массива байтов byte[]

import com.example.pokerclub.common.BadRequestException;
import com.example.pokerclub.enrollment.Enrollment;
import com.example.pokerclub.enrollment.EnrollmentRepository;
import com.example.pokerclub.enrollment.EnrollmentStatus;
import com.example.pokerclub.event.Event;
import com.example.pokerclub.event.EventService;
import com.example.pokerclub.event.EventType;
import com.example.pokerclub.rating.RatingEntry;
import com.example.pokerclub.rating.RatingEntryRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.pokerclub.user.User;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;



@Service
public class ReportService {

    private final EventService eventService; //нужен чтобы найти мероприятие по id
    private final RatingEntryRepository ratingEntryRepository; //нужен дляполучения отчета  турнире: кто сколько очков получил
    private final EnrollmentRepository enrollmentRepository;

    public ReportService(EventService eventService,
                         RatingEntryRepository ratingEntryRepository,
                         EnrollmentRepository enrollmentRepository) {
        this.eventService = eventService;
        this.ratingEntryRepository = ratingEntryRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    //формирует CSV отчет
    @Transactional(readOnly = true) //значит что мы только читаем данные и ничег оне изменяем
    //Long tournamentId - id турнира, по которому надо сформировать отчет.
    //byte[] - результат работы метода. Это готовый файл в виде байтов.
    private record ReportRow(User visitor, int points) {
    }
    public byte[] generateTournamentCsvReport(Long tournamentId) {
        Event tournament = findTournament(tournamentId); //берем мероприятие и сразу прверяем что это турнир
        List<ReportRow> rows = buildReportRows(tournamentId); //получаем все начисления очков по конкретному турниру
        //метод возвращает список отсортированный по очка по убыанию

        StringBuilder csv = new StringBuilder(); //нужен чтобы постепенно собрать текст файла CSV
        //не String потому что если очень много добавлять к тексту потом еще текс, то будут создаваться много новых строк
        //а StringBuilder постепенно собирает текст
        csv.append('\uFEFF'); // для открытия русских символов пи открытии файла

        //добавление первой строки
       //\n для перехода на другую строку
       // ; - для разделителя колонок
        csv.append("Tournament ID;Title;DateTime\n");


        //\n\n - два перехода на новую строку, чтобы отделить информацию о турнире от таблицы результатов.
        //escapeCsv(tournament.getTitle()) нужен, чтобы название турнира не сломало CSV.
        //если вдруг там встретится ; то наодо чтобы CSV не подумал что это перенос столбца поэтому escapeCsv
        csv.append(tournament.getId())
                .append(";")
                .append(escapeCsv(tournament.getTitle()))
                .append(";")
                .append(tournament.getDateTime())
                .append("\n\n");

        //заголовок таблицы результатов
        csv.append("Place;Visitor ID;First Name;Last Name;Email;Points\n");

        //проходим по всем результатам турнира
        for (int i = 0; i < rows.size(); i++) {
            ReportRow row = rows.get(i);

            csv.append(i + 1)
                    .append(";")
                    .append(row.visitor().getId())
                    .append(";")
                    .append(escapeCsv(row.visitor().getFirstName()))
                    .append(";")
                    .append(escapeCsv(row.visitor().getLastName()))
                    .append(";")
                    .append(escapeCsv(row.visitor().getEmail()))
                    .append(";")
                    .append(row.points())
                    .append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8); //текст CSV превращаетя в байты
        //StandardCharsets.UTF_8 - для русских символов
    }

    //формирует pdf отчет
    //принимает id турнира, читает данные и возвращает файл в виде byte[]
    @Transactional(readOnly = true)
    public byte[] generateTournamentPdfReport(Long tournamentId) {
        Event tournament = findTournament(tournamentId); //проверка что это турнир
        List<ReportRow> rows = buildReportRows(tournamentId); //получаени результата турнира

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); //поток в памяти
        //файл не создается на диске, он собирается в оперативке и потом передается через HTTP

        Document document = new Document(); //объект pdf документа
        PdfWriter.getInstance(document, outputStream); //объект который умеет записывать содержимое Document в потом байтов

        Font titleFont = createPdfFont(16, Font.BOLD);
        Font textFont = createPdfFont(12, Font.NORMAL);

        document.open();

        //добавление текстовых блоков
        //Paragraph - это абзац
        document.add(new Paragraph("Отчет по турниру"));
        document.add(new Paragraph("ID турнира: " + tournament.getId()));
        document.add(new Paragraph("Название: " + tournament.getTitle()));
        document.add(new Paragraph("Дата и время: " + tournament.getDateTime()));
        document.add(new Paragraph(" "));

        if (rows.isEmpty()) {
            document.add(new Paragraph("По турниру пока нет подтвержденных участников.", textFont));
        } else {
            document.add(new Paragraph("Результаты:", titleFont));

            for (int i = 0; i < rows.size(); i++) {
                ReportRow row = rows.get(i);

                String line = (i + 1) + ". "
                        + row.visitor().getFirstName() + " "
                        + row.visitor().getLastName()
                        + " (" + row.visitor().getEmail() + ")"
                        + " - " + row.points() + " очков";

                document.add(new Paragraph(line, textFont));
            }

        }

        document.close();

        return outputStream.toByteArray(); // берем готовые байты из PDF и возвращаем их
    }

    //проверяет что отчет формируется именно для турнира а не для тренировки
    private Event findTournament(Long tournamentId) {
        Event tournament = eventService.findEvent(tournamentId);

        if (tournament.getEventType() != EventType.TOURNAMENT) {
            throw new BadRequestException("Отчет можно сформировать только для турнира");
        }

        return tournament;
    }

    //для защиты от ошибок в CSV
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\"", "\"\"")
                .replace(";", ",")
                .replace("\n", " ")
                .replace("\r", " ");
        //replace("\"", "\"\"") - если внутри текста есть кавычка, заменяем ее на две кавычки.
        //replace(";", ",") - если внутри текста есть ;, заменяем на запятую, потому что ; у разделяет колонки.
        //replace("\n", " ") - убираем перенос строки.
        //replace("\r", " ") - убираем еще один тип переноса строки.
    }

    private Font createPdfFont(float size, int style) {
        try {
            BaseFont baseFont = BaseFont.createFont(
                    "C:/Windows/Fonts/arial.ttf",
                    BaseFont.IDENTITY_H, //режим кодировки, который позволяет норм выводить русские символы
                    BaseFont.EMBEDDED //шифр встраивается внутрь pdf файла - его можно будет нормально открыть на друго компе
            );

            return new Font(baseFont, size, style);
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось загрузить шрифт для PDF", e);
        }
    }

    private List<ReportRow> buildReportRows(Long tournamentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByEventIdOrderByCreatedAtAsc(tournamentId)
                .stream()
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.PRESENT)
                .toList();

        Map<Long, RatingEntry> entriesByVisitorId = ratingEntryRepository.findByTournamentIdOrderByPointsDesc(tournamentId)
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getVisitor().getId(),
                        Function.identity()
                ));

        return enrollments.stream()
                .map(enrollment -> {
                    RatingEntry entry = entriesByVisitorId.get(enrollment.getVisitor().getId());
                    int points = entry == null ? 0 : entry.getPoints();
                    return new ReportRow(enrollment.getVisitor(), points);
                })
                .sorted((a, b) -> Integer.compare(b.points(), a.points()))
                .toList();
    }
}