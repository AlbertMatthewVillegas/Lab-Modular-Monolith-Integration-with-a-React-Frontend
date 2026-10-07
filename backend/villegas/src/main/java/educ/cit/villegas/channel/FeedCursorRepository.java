package educ.cit.villegas.channel;

import org.springframework.data.jpa.repository.JpaRepository;

interface FeedCursorRepository extends JpaRepository<FeedCursor, Integer> {
}
