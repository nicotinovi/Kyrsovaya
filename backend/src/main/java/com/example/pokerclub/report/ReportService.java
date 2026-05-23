package com.example.pokerclub.report;

//собирает данные и возвращает файл в виде массива байтов byte[]

import com.example.pokerclub.common.BadRequestException;
import com.example.pokerclub.event.Event;
import com.example.pokerclub.event.EventService;
import com.example.pokerclub.event.EventType;
import com.example.pokerclub.rating.RatingEntry;
import com.example.pokerclub.rating.RatingEntryRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;



@Service
public class ReportService {

    private final EventService eventService; //нужен чтобы найти мероприятие по id
    private final RatingEntryRepository ratingEntryRepository; //нужен дляполучения отчета  турнире: кто сколько очков получил

    public ReportService(EventService eventService,
                         RatingEntryRepository ratingEntryRepository) {
        this.eventService = eventService;
        this.ratingEntryRepository = ratingEntryRepository;
    }

    //формирует CSV отчет
    @Transactional(readOnly = true) //значит что мы только читаем данные и ничег оне изменяем
    //Long tournamentId - id турнира, по которому надо сформировать отчет.
    //byte[] - результат работы метода. Это готовый файл в виде байтов.
    public byte[] generateTournamentCsvReport(Long tournamentId) {
        Event tournament = findTournament(tournamentId); //берем мероприятие и сразу прверяем что это турнир
        List<RatingEntry> entries = ratingEntryRepository.findByTournamentIdOrderByPointsDesc(tournamentId); //получаем все начисления очков по конкретному турниру
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
        for (int i = 0; i < entries.size(); i++) {
            RatingEntry entry = entries.get(i);

            csv.append(i + 1)
                    //entry.getVisitor() - игрок, которому начислены очки.
                    //entry.getPoints() - очки игрока за этот конкретный турнир.
                    .append(";")
                    .append(entry.getVisitor().getId())
                    .append(";")
                    .append(escapeCsv(entry.getVisitor().getFirstName()))
                    .append(";")
                    .append(escapeCsv(entry.getVisitor().getLastName()))
                    .append(";")
                    .append(escapeCsv(entry.getVisitor().getEmail()))
                    .append(";")
                    .append(entry.getPoints())
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
        List<RatingEntry> entries = ratingEntryRepository.findByTournamentIdOrderByPointsDesc(tournamentId); //получаени результата турнира

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

        if (entries.isEmpty()) { //если нет результатов турнира, то в пдф будет сообщение ниже
            document.add(new Paragraph("По турниру пока нет начислений рейтинга."));
        } else { //если есть результат то добавляем заголовок Result
            document.add(new Paragraph("Результаты:"));

            //проход по каждому результату турнира
            for (int i = 0; i < entries.size(); i++) {
                RatingEntry entry = entries.get(i);

                String line = (i + 1) + ". "
                        + entry.getVisitor().getFirstName() + " "
                        + entry.getVisitor().getLastName()
                        + " (" + entry.getVisitor().getEmail() + ")"
                        + " - " + entry.getPoints() + " очков";

                document.add(new Paragraph(line)); //добавление строки в пдф
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
}