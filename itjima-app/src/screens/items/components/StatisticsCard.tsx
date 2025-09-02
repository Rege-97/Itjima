import { MaterialCommunityIcons } from '@expo/vector-icons';
import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Card, Text } from 'react-native-paper';

type IconName = keyof typeof MaterialCommunityIcons.glyphMap;

interface StatisticsCardProps {
  title: string;
  value: string;
  iconName: IconName;
  iconColor: string;
}

const StatisticsCard: React.FC<StatisticsCardProps> = ({ title, value, iconName, iconColor }) => {
  return (
    <Card style={styles.card}>
      <Card.Content style={styles.content}>
        <MaterialCommunityIcons name={iconName} size={30} color={iconColor} style={styles.icon} />
        <Text style={styles.title}>{title}</Text>
        <Text style={styles.value}>{value}</Text>
      </Card.Content>
    </Card>
  );
};

const styles = StyleSheet.create({
  card: {
    flex: 1,
    marginHorizontal: 4,
    borderRadius: 15,
    elevation: 3,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    backgroundColor: '#fff',
  },
  content: {
    alignItems: 'center',
    paddingVertical: 15,
  },
  icon: {
    marginBottom: 8,
  },
  title: {
    fontSize: 14,
    color: '#666',
    marginBottom: 4,
  },
  value: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#333',
  },
});

export default StatisticsCard;