//API для отчетов
const API_BASE_URL = 'http://localhost:8080/api';

function downloadFile(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');

  link.href = url;
  link.download = filename;
  link.click();

  URL.revokeObjectURL(url);
}

export async function downloadTournamentCsvReport(tournamentId: number, token: string) {
  const response = await fetch(
    `${API_BASE_URL}/admin/tournaments/${tournamentId}/reports.csv`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );

  if (!response.ok) {
    throw new Error('Не удалось скачать CSV-отчет');
  }

  const blob = await response.blob();
  downloadFile(blob, `tournament-${tournamentId}-report.csv`);
}

export async function downloadTournamentPdfReport(tournamentId: number, token: string) {
  const response = await fetch(
    `${API_BASE_URL}/admin/tournaments/${tournamentId}/reports.pdf`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );

  if (!response.ok) {
    throw new Error('Не удалось скачать PDF-отчет');
  }

  const blob = await response.blob();
  downloadFile(blob, `tournament-${tournamentId}-report.pdf`);
}