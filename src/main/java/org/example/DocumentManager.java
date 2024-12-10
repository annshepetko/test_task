package org.example;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {


    private static Map<String, Document> store = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {

        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }


        Document newDocument = buildDocument(document);

        store.put(newDocument.id, newDocument);
        return newDocument;
    }

    private String generateId(Document document) {

        String id = document.id;

        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        while (store.containsKey(id)) {          // if id already exists we generate a new id
            id = UUID.randomUUID().toString();
        }

        return id;
    }

    private Document buildDocument(Document document) {

        String id = generateId(document);
        Instant created = Optional.ofNullable(document.getCreated()).orElse(Instant.now());


        Document newDocument = Document.builder()
                .id(id)
                .title(document.getTitle())
                .content(document.getContent())
                .author(document.getAuthor())
                .created(created)
                .build();

        return newDocument;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {

        return store.values().stream()
                .filter((document) -> matchTitles(document.title, request.titlePrefixes))
                .filter((document -> matchContent(document.content, request.containsContents)))
                .filter(document -> matchAuthors(document.id, request.authorIds))
                .filter(document -> matchesCreatedRange(document, request.createdFrom, request.createdTo))
                .toList();
    }

    private boolean matchTitles(String documentTitle, List<String> requestTitles) {

        if (documentTitle == null || requestTitles.isEmpty()) {
            return true;
        }

        return requestTitles.stream().anyMatch(documentTitle::startsWith);
    }

    private boolean matchContent(String documentContent, List<String> contents) {

        if (documentContent == null || contents.isEmpty()) {
            return true;
        }
        return contents.stream().anyMatch(documentContent::contains);
    }

    private boolean matchAuthors(String authorId, List<String> authors) {
        if (authors.isEmpty() || authorId == null) {
            return true;
        }
        return authors.stream().anyMatch(a -> a.equals(authorId));
    }

    private boolean matchesCreatedRange(Document document, Instant from, Instant to) {
        Instant created = document.getCreated();
        if (from != null && created.isBefore(from)) {
            return false;
        }
        if (to != null && created.isAfter(to)) {
            return false;
        }
        return true;
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {

        return Optional.ofNullable(store.get(id));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
