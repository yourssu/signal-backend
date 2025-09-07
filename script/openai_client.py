import os
import json
import requests
from typing import Dict, Any, Optional
from dotenv import load_dotenv


class OpenAIClient:
    def __init__(self):
        load_dotenv(override=True)
        self.api_key = os.getenv('OPENAI_API_KEY')
        self.base_url = os.getenv('OPENAI_URL', 'https://api.openai.com/v1/responses')
        self.model = os.getenv('OPENAI_MODEL', 'gpt-5-mini')
        self.policy_prompt = os.getenv('OPENAI_POLICY_PROMPT')
    
    def check_policy_violation(self, profile_data: Dict[str, Any]) -> Dict[str, Any]:
        try:
            profile_text = self._format_profile_for_check(profile_data)
            response = self._make_openai_request(profile_text)
            return self._parse_violation_response(response)
        except Exception as e:
            return {
                'violation': False,
                'reason': f'Policy check failed: {str(e)}'
            }
    
    def _format_profile_for_check(self, profile_data: Dict[str, Any]) -> str:
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
        self._validate_error(response_data)
        return self._parse_openai_response(response_data)
    
    def _validate_error(self, response_data: dict):
        error = response_data.get('error')
        if error is not None:
            error_message = error.get('message', 'Unknown OpenAI error') if isinstance(error, dict) else 'Unknown OpenAI error'
            raise Exception(f"OpenAI API Error: {error_message}")
    
    def _parse_openai_response(self, response_data: dict) -> str:
        try:
            output = response_data.get('output')
            if not output or not isinstance(output, list):
                raise Exception("No output array found in response")
            
            assistant_msg = None
            for item in output:
                if isinstance(item, dict) and item.get('type') == 'message':
                    if item.get('status') == 'completed':
                        assistant_msg = item
                        break
            
            if not assistant_msg:
                raise Exception("No completed message type found in output")
            
            content = assistant_msg.get('content')
            if not content or not isinstance(content, list):
                raise Exception("No content array found in message")
            
            text_part = None
            for item in content:
                if isinstance(item, dict) and item.get('type') == 'output_text':
                    text_part = item
                    break
            
            if not text_part:
                raise Exception("No output_text type found in content")
            
            text = text_part.get('text')
            if not text:
                raise Exception("No text found in output_text")
            
            return text
            
        except Exception as e:
            raise Exception(f"Failed to parse OpenAI response: {str(e)}")
    
    def _parse_violation_response(self, response_text: str) -> Dict[str, Any]:
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


openai_client = OpenAIClient()
