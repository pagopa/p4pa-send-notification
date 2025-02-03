package it.gov.pagopa.pu.send.service;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import it.gov.pagopa.pu.send.model.CustomSequenceGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;

@ExtendWith(MockitoExtension.class)
class SequenceGeneratorServiceTest {

  @Mock
  private MongoOperations mongoOperationsMock;

  @InjectMocks
  private SequenceGeneratorService sequenceGeneratorService;

  @Test
  void givenValidSequenceNameWhenGenerateSequenceThenReturnNewValue() {
    // Given
    String seqName = "testSequence";
    CustomSequenceGenerator mockSequence = new CustomSequenceGenerator();
    mockSequence.setSeq(99L);

    Mockito.when(mongoOperationsMock.findAndModify(
      Mockito.eq(query(where("_id").is(seqName))),
      Mockito.any(Update.class),
      Mockito.any(FindAndModifyOptions.class),
      Mockito.eq(CustomSequenceGenerator.class)
    )).thenReturn(mockSequence);

    // When
    long result = sequenceGeneratorService.generateSequence(seqName);

    // Then
    Assertions.assertEquals(99L, result);
  }
}
