package educ.cit.villegas.shop;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class OrderException extends ResponseStatusException {
    OrderException(HttpStatus status, String reason) {
        super(status, reason);
    }
}
