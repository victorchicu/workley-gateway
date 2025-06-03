package app.awaytogo.gateway.resume.domain;

public enum ResumeStatus {
    /*
        The very first state when the creation request is received but before any significant processing (like event persistence) has begun. Might be very short-lived.
     */
    INITIALIZING,
    /*
        The resume creation has been initiated, and the system is about to or is currently attempting to fetch data from the provided LinkedIn URL.
     */
    PENDING_PROFILE_FETCH,
    /*
        The system failed to fetch or parse data from the LinkedIn URL (e.g., invalid URL, profile private, network error, parsing error).
        Triggered by: LinkedInDataProcessingFailedEvent (or a more specific fetch failure event).
     */
    PROFILE_FETCH_FAILED,
    /*
        LinkedIn data has been successfully fetched and is now being processed or transformed into your internal resume data structure. (Optional, if fetching and initial processing are distinct steps).
     */
    PROFILE_DATA_PROCESSING,
    /*
        LinkedIn profile data has been successfully fetched and processed. The system is now waiting for the user to select a PDF template.
        Triggered by: LinkedInDataFetchedEvent (or ProfileDataProcessedEvent).
     */
    AWAITING_TEMPLATE_SELECTION,
    /*
        The user has selected a template, and the system is about to or is currently preparing for PDF generation.
        Triggered by: ResumeTemplateSelectedEvent.
     */
    TEMPLATE_SELECTED,
    /*
        The system is actively generating the PDF document based on the resume data and the selected template.
        Triggered by: An internal event or directly after ResumeTemplateSelectedEvent if PDF generation starts immediately.
     */
    PDF_GENERATING,
    /*
        The system encountered an error while trying to generate the PDF.
        Triggered by: PdfGenerationFailedEvent (or similar).
     */
    PDF_GENERATION_FAILED,
    /*
        The PDF has been successfully generated and is available for the user to download. This is a final success state for a given version.
        Triggered by: ResumePdfGeneratedEvent.
     */
    READY_FOR_DOWNLOAD,
    /*
        A more generic failure state if an error occurs that doesn't fit neatly into the more specific failure statuses above, or if a critical unrecoverable error happens at any stage.
        Triggered by: ResumeProcessingFailedEvent or other unhandled exceptions.
     */
    PROCESSING_FAILED,
    /*
        Optional
     */
    ARCHIVED,
    DELETED
}
