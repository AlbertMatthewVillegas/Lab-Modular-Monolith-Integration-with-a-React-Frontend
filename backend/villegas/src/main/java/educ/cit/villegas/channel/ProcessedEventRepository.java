package educ.cit.villegas.channel;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {
}
