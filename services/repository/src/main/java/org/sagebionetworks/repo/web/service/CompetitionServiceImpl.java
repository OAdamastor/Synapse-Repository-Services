package org.sagebionetworks.repo.web.service;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.sagebionetworks.competition.manager.CompetitionManager;
import org.sagebionetworks.competition.manager.ParticipantManager;
import org.sagebionetworks.competition.manager.SubmissionManager;
import org.sagebionetworks.competition.model.Competition;
import org.sagebionetworks.competition.model.Participant;
import org.sagebionetworks.competition.model.Submission;
import org.sagebionetworks.competition.model.SubmissionStatus;
import org.sagebionetworks.competition.model.SubmissionStatusEnum;
import org.sagebionetworks.repo.manager.UserManager;
import org.sagebionetworks.repo.model.ConflictingUpdateException;
import org.sagebionetworks.repo.model.DatastoreException;
import org.sagebionetworks.repo.model.InvalidModelException;
import org.sagebionetworks.repo.model.PaginatedResults;
import org.sagebionetworks.repo.model.QueryResults;
import org.sagebionetworks.repo.model.UnauthorizedException;
import org.sagebionetworks.repo.model.UserInfo;
import org.sagebionetworks.repo.web.NotFoundException;
import org.sagebionetworks.repo.web.UrlHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class CompetitionServiceImpl implements CompetitionService {
	
	@Autowired
	CompetitionManager competitionManager;
	@Autowired
	ParticipantManager participantManager;
	@Autowired
	SubmissionManager submissionManager;
	@Autowired
	UserManager userManager;
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Competition createCompetition(String userName, Competition comp) 
			throws DatastoreException, InvalidModelException, NotFoundException {
		UserInfo userInfo = userManager.getUserInfo(userName);
		return competitionManager.createCompetition(userInfo, comp);
	}
	
	@Override
	public Competition getCompetition(String id) throws DatastoreException,
			NotFoundException, UnauthorizedException {
		return competitionManager.getCompetition(id);
	}

	@Override
	public PaginatedResults<Competition> getCompetitionsInRange(long limit, long offset, HttpServletRequest request) 
			throws DatastoreException, NotFoundException {
		QueryResults<Competition> res = competitionManager.getInRange(limit, offset);
		return new PaginatedResults<Competition>(
				request.getServletPath() + UrlHelpers.COMPETITION,
				res.getResults(),
				(int) res.getTotalNumberOfResults(),
				1,
				(int) res.getTotalNumberOfResults(),
				"",
				false				
			);
	}

	@Override
	public long getCompetitionCount() throws DatastoreException, NotFoundException {
		return competitionManager.getCount();
	}

	@Override
	public Competition findCompetition(String name)
			throws DatastoreException, NotFoundException, UnauthorizedException {
		return competitionManager.findCompetition(name);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Competition updateCompetition(String userName, Competition comp)
			throws DatastoreException, NotFoundException, UnauthorizedException,
			InvalidModelException, ConflictingUpdateException {
		UserInfo userInfo = userManager.getUserInfo(userName);
		return competitionManager.updateCompetition(userInfo, comp);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void deleteCompetition(String userName, String id)
			throws DatastoreException, NotFoundException, UnauthorizedException {
		UserInfo userInfo = userManager.getUserInfo(userName);
		competitionManager.deleteCompetition(userInfo, id);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Participant addParticipant(String userName, String compId) throws NotFoundException {
		UserInfo userInfo = userManager.getUserInfo(userName);
		return participantManager.addParticipant(userInfo, compId);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Participant addParticipantAsAdmin(String userName, String compId,
			String idToAdd) throws NotFoundException {
		UserInfo userInfo = userManager.getUserInfo(userName);
		return participantManager.addParticipantAsAdmin(userInfo, compId, idToAdd);
	}

	@Override
	public Participant getParticipant(String principalId, String compId)
			throws DatastoreException, NotFoundException {
		return participantManager.getParticipant(principalId, compId);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void removeParticipant(String userName, String compId,
			String idToRemove) throws DatastoreException, NotFoundException {
		UserInfo userInfo = userManager.getUserInfo(userName);
		participantManager.removeParticipant(userInfo, compId, idToRemove);
	}

	@Override
	public List<Participant> getAllParticipants(String compId)
			throws NumberFormatException, DatastoreException, NotFoundException {
		return participantManager.getAllParticipants(compId);
	}

	@Override
	public long getParticipantCount(String compId)
			throws DatastoreException, NotFoundException {
		return participantManager.getNumberofParticipants(compId);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Submission createSubmission(String userName, Submission submission)
			throws NotFoundException {
		UserInfo userInfo = userManager.getUserInfo(userName);
		return submissionManager.createSubmission(userInfo, submission);
	}

	@Override
	public Submission getSubmission(String submissionId)
			throws DatastoreException, NotFoundException {
		return submissionManager.getSubmission(submissionId);
	}

	@Override
	public SubmissionStatus getSubmissionStatus(String submissionId)
			throws DatastoreException, NotFoundException {
		return submissionManager.getSubmissionStatus(submissionId);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public SubmissionStatus updateSubmissionStatus(String userName,
			SubmissionStatus submissionStatus) throws NotFoundException {
		UserInfo userInfo = userManager.getUserInfo(userName);
		return submissionManager.updateSubmissionStatus(userInfo, submissionStatus);
	}

	@Override
	@Deprecated
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void deleteSubmission(String userName, String submissionId)
			throws DatastoreException, NotFoundException {
		UserInfo userInfo = userManager.getUserInfo(userName);
		submissionManager.deleteSubmission(userInfo, submissionId);
	}

	@Override
	public List<Submission> getAllSubmissions(String userName, String compId,
			SubmissionStatusEnum status) throws DatastoreException,
			UnauthorizedException, NotFoundException {
		UserInfo userInfo = userManager.getUserInfo(userName);
		return submissionManager.getAllSubmissions(userInfo, compId, status);
	}

	@Override
	public List<Submission> getAllSubmissionsByUser(String princpalId)
			throws DatastoreException, NotFoundException {
		return submissionManager.getAllSubmissionsByUser(princpalId);
	}

	@Override
	public long getSubmissionCount(String compId) throws DatastoreException,
			NotFoundException {
		return submissionManager.getSubmissionCount(compId);
	}
	
}