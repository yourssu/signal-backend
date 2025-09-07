"""
OpenAI 클라이언트 - 프로필 정책 위반 검사
"""
import os
import json
import requests
from typing import Dict, Any, Optional


class OpenAIClient:
    def __init__(self):
        self.api_key = os.getenv('OPENAI_API_KEY')
        self.base_url = 'https://api.openai.com/v1/responses'
        self.model = os.getenv('OPENAI_MODEL')
        self.policy_prompt = os.getenv('OPENAI_POLICY_PROMPT')
    
    def check_policy_violation(self, profile_data: Dict[str, Any]) -> Dict[str, Any]:
        """
        프로필 데이터의 정책 위반 여부를 확인합니다.
        
        Args:
            profile_data: 프로필 정보 딕셔너리
            
        Returns:
            Dict containing 'violation' (bool) and 'reason' (str, optional)
        """
        try:
            # 프로필 정보를 텍스트로 변환
            profile_text = self._format_profile_for_check(profile_data)
            
            # OpenAI API 요청
            response = self._make_openai_request(profile_text)
            
            # 응답 파싱
            return self._parse_violation_response(response)
            
        except Exception as e:
            print(f"Error checking policy violation: {str(e)}")
            return {
                'violation': False,
                'reason': f'Policy check failed: {str(e)}'
            }
    
    def _format_profile_for_check(self, profile_data: Dict[str, Any]) -> str:
        """프로필 데이터를 OpenAI 검사용 텍스트로 변환"""
        formatted_lines = [
            f"학과: {profile_data.get('department', 'N/A')}",
            f"연락처: {profile_data.get('contact', 'N/A')}",
            f"닉네임: {profile_data.get('nickname', 'N/A')}",
            f"자기소개: {profile_data.get('introSentences', 'N/A')}"
        ]
        return "\n".join(formatted_lines)
    
    def _make_openai_request(self, profile_text: str) -> str:
        headers = {
            'Content-Type': 'application/json',
            'Authorization': f'Bearer {self.api_key}'
        }
        
        input_items = [
            {
                'role': 'developer',
                'content': self.policy_prompt
            },
            {
                'role': 'user', 
                'content': profile_text
            }
        ]
        
        payload = {
            'model': self.model,
            'reasoning': {
                'effort': 'low'
            },
            'input': input_items
        }
        
        response = requests.post(
            self.base_url,
            headers=headers,
            json=payload,
            timeout=30
        )
        
        if response.status_code != 200:
            raise Exception(f"OpenAI API error: {response.status_code} - {response.text}")
        
        response_data = response.json()
        
        # Kotlin과 동일한 에러 검증
        self._validate_error(response_data)
        
        # Kotlin과 동일한 응답 파싱
        return self._parse_openai_response(response_data)
    
    def _validate_error(self, response_data: dict):
        """Kotlin OpenAIModel의 validateError와 동일한 에러 검증"""
        error = response_data.get('error')
        if error is not None:
            error_message = error.get('message', 'Unknown OpenAI error') if isinstance(error, dict) else 'Unknown OpenAI error'
            raise Exception(f"OpenAI API Error: {error_message}")
    
    def _parse_openai_response(self, response_data: dict) -> str:
        """Kotlin OpenAIModel의 parse 메서드와 동일한 응답 파싱"""
        try:
            # val output = root["output"]?.jsonArray ?: throw FailedOpenAIModelException()
            output = response_data.get('output')
            if not output or not isinstance(output, list):
                raise Exception("No output array found in response")
            
            # val assistantMsg = output.find { it.jsonObject["type"]?.jsonPrimitive?.content == "message" }
            assistant_msg = None
            for item in output:
                if isinstance(item, dict) and item.get('type') == 'message':
                    assistant_msg = item
                    break
            
            if not assistant_msg:
                raise Exception("No message type found in output")
            
            # val content = assistantMsg["content"]?.jsonArray ?: throw FailedOpenAIModelException()
            content = assistant_msg.get('content')
            if not content or not isinstance(content, list):
                raise Exception("No content array found in message")
            
            # val textPart = content.find { it.jsonObject["type"]?.jsonPrimitive?.content == "output_text" }
            text_part = None
            for item in content:
                if isinstance(item, dict) and item.get('type') == 'output_text':
                    text_part = item
                    break
            
            if not text_part:
                raise Exception("No output_text type found in content")
            
            # return textPart["text"]?.jsonPrimitive?.content ?: throw FailedOpenAIModelException()
            text = text_part.get('text')
            if not text:
                raise Exception("No text found in output_text")
                
            return text
            
        except Exception as e:
            raise Exception(f"Failed to parse OpenAI response: {str(e)}")
    
    def _parse_violation_response(self, response_text: str) -> Dict[str, Any]:
        """OpenAI 응답을 파싱하여 위반 여부와 사유를 추출합니다"""
        lines = [line.strip() for line in response_text.split('\n') if line.strip()]
        
        violation = False
        reason = None
        
        for line in lines:
            if line.startswith('VIOLATION:'):
                violation_str = line.split(':', 1)[1].strip().lower()
                violation = violation_str == 'true'
            elif line.startswith('REASON:') and violation:
                reason = line.split(':', 1)[1].strip()
        
        result = {'violation': violation}
        if reason:
            result['reason'] = reason
            
        return result


# 싱글톤 인스턴스
openai_client = OpenAIClient()
