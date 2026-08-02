package com.passro.passrobackend.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

class SwaggerSchemaIntegrationTest extends IntegrationTestSupport {

    @Test
    void senderDetailResponseUsesObjectDtoSchema() throws Exception {
        String body = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode document = objectMapper.readTree(body);

        assertThat(document.at(
                "/paths/~1sender~1{deliveryId}/get/responses/200/content/*~1*/schema/$ref").asText())
                .isEqualTo("#/components/schemas/APIResponseSenderDeliveryDetailDto");
        assertThat(document.at(
                "/components/schemas/APIResponseSenderDeliveryDetailDto/properties/result/$ref").asText())
                .isEqualTo("#/components/schemas/SenderDeliveryDetailDto");
        assertThat(document.at(
                "/components/schemas/SenderDeliveryDetailDto/type").asText())
                .isEqualTo("object");
    }
}
