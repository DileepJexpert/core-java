import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/models.dart';

class ApiService {
  final String baseUrl;
  final http.Client _client;

  ApiService({this.baseUrl = 'http://localhost:8080'})
      : _client = http.Client();

  Future<List<ServiceHealth>> fetchServices() async {
    final response = await _client.get(
      Uri.parse('$baseUrl/api/dashboard/summary'),
    );
    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((e) => ServiceHealth.fromJson(e)).toList();
    }
    throw ApiException('Failed to fetch services: ${response.statusCode}');
  }

  Future<List<Incident>> fetchActiveIncidents() async {
    final response = await _client.get(
      Uri.parse('$baseUrl/api/dashboard/incidents/active'),
    );
    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((e) => Incident.fromJson(e)).toList();
    }
    throw ApiException('Failed to fetch incidents: ${response.statusCode}');
  }

  Future<List<RemediationLog>> fetchRemediationLogs({int size = 20}) async {
    final response = await _client.get(
      Uri.parse('$baseUrl/api/remediation/log?size=$size'),
    );
    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((e) => RemediationLog.fromJson(e)).toList();
    }
    throw ApiException(
        'Failed to fetch remediation logs: ${response.statusCode}');
  }

  Future<DashboardStats> fetchStats() async {
    final response = await _client.get(
      Uri.parse('$baseUrl/api/dashboard/stats'),
    );
    if (response.statusCode == 200) {
      return DashboardStats.fromJson(jsonDecode(response.body));
    }
    throw ApiException('Failed to fetch stats: ${response.statusCode}');
  }

  Future<void> resolveIncident(String id) async {
    final response = await _client.post(
      Uri.parse('$baseUrl/api/incidents/$id/resolve'),
    );
    if (response.statusCode != 200) {
      throw ApiException('Failed to resolve incident: ${response.statusCode}');
    }
  }

  Future<List<AlertRule>> fetchRules() async {
    final response = await _client.get(
      Uri.parse('$baseUrl/api/rules'),
    );
    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((e) => AlertRule.fromJson(e)).toList();
    }
    throw ApiException('Failed to fetch rules: ${response.statusCode}');
  }

  Future<void> createRule(AlertRule rule) async {
    final response = await _client.post(
      Uri.parse('$baseUrl/api/rules'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(rule.toJson()),
    );
    if (response.statusCode != 200 && response.statusCode != 201) {
      throw ApiException('Failed to create rule: ${response.statusCode}');
    }
  }

  Future<void> deleteRule(String id) async {
    final response = await _client.delete(
      Uri.parse('$baseUrl/api/rules/$id'),
    );
    if (response.statusCode != 200 && response.statusCode != 204) {
      throw ApiException('Failed to delete rule: ${response.statusCode}');
    }
  }

  void dispose() {
    _client.close();
  }
}

class ApiException implements Exception {
  final String message;
  const ApiException(this.message);

  @override
  String toString() => 'ApiException: $message';
}
