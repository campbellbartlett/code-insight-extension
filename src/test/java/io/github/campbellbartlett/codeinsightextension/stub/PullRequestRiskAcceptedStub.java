package io.github.campbellbartlett.codeinsightextension.stub;

import io.github.campbellbartlett.codeinsightextension.activeobjects.PullRequestRiskAccepted;
import lombok.NoArgsConstructor;
import net.java.ao.EntityManager;
import net.java.ao.RawEntity;

import java.beans.PropertyChangeListener;
import java.util.Date;

@NoArgsConstructor
public class PullRequestRiskAcceptedStub implements PullRequestRiskAccepted {

    private String authenticationUserSlug;

    public PullRequestRiskAcceptedStub(String authenticationUserSlug) {
        this.authenticationUserSlug = authenticationUserSlug;
    }

    @Override
    public String getCommitHash() {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Override
    public void setCommitHash(String commitHash) {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Override
    public String getRepositorySlug() {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Override
    public void setRepositorySlug(String repoSlug) {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Override
    public String getProjectId() {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Override
    public void setProjectId(String projectId) {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Override
    public String getAuthenticatingUserSlug() {
        return authenticationUserSlug;
    }

    @Override
    public void setAuthenticatingUserSlug(String adminSlug) {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Override
    public Date getAcceptedDate() {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Override
    public void setAcceptedDate(Date date) {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Override
    public boolean getRevoked() {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Override
    public void setRevoked(boolean revoked) {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Override
    public int getID() {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Override
    public void init() {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Override
    public void save() {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Override
    public EntityManager getEntityManager() {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Override
    public <X extends RawEntity<Integer>> Class<X> getEntityType() {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }
}
