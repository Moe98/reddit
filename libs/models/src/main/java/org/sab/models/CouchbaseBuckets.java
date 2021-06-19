package org.sab.models;

public enum CouchbaseBuckets {
    LISTINGS("Listings"),
    RECOMMENDED_SUB_THREADS("RecommendedSubThreads"),
    RECOMMENDED_THREADS("RecommendedThreads"),
    RECOMMENDED_USERS("RecommendedUsers"),
    THREADS("Threads"),
    SUBTHREADS("SubThreads"),
    COMMENTS("Comments");


    private final String bucketName;

    CouchbaseBuckets(String bucketName) {
        this.bucketName = bucketName;
    }

    public String get() {
        return this.bucketName;
    }
}
