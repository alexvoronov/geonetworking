package net.gcdc.camdenm;

import net.gcdc.asn1.datatypes.Asn1Integer;
import net.gcdc.asn1.datatypes.Asn1Optional;
import net.gcdc.asn1.datatypes.IntRange;
import net.gcdc.asn1.datatypes.Sequence;
import net.gcdc.camdenm.CoopIts.ItsPduHeader;
import net.gcdc.camdenm.CoopIts.StationID;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.MessageId;
import net.gcdc.camdenm.CoopIts.GenerationDeltaTime;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.ProtocolVersion;

/*
 * Copyright 2015 TNO

 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *	software distributed under the License is distributed on an
 *	"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *	KIND, either express or implied.  See the License for the
 *	specific language governing permissions and limitations
 *	under the License.
 */
 
/**
 * i-GAME cooperative lane change message implementation
 * Signal specification version: V1.0 for I-GAME @see <a href="http://www.gcdc.net/i-game">GCDC</a>
 * 
 *
 * This implementation was partly developed within i-GAME project that has received funding from the
 * European Union's Seventh Framework Programme for research, technological development and demonstration
 * under grant agreement no 612035.
 *
 */
public final  class Iclcm {
	/**
	 * MessageID of i-GAME cooperative lane change message
	 */
	public static final int MessageID_iCLCM = 10;
	private Iclcm(){}
	
	@Sequence
    public static class IgameCooperativeLaneChangeMessage {
        ItsPduHeader header;
        IgameCooperativeLaneChangeMessageBody iclm;

        public IgameCooperativeLaneChangeMessage(ItsPduHeader itsPduHeader, IgameCooperativeLaneChangeMessageBody iclm) {
            this.header = itsPduHeader;
            this.iclm = iclm;
        }

        public IgameCooperativeLaneChangeMessage() { 
        	this(
        			new ItsPduHeader(new ProtocolVersion(1),new MessageId(MessageID_iCLCM),new StationID()),
        			new IgameCooperativeLaneChangeMessageBody()
        			); 
        }

        @Override public String toString() { return "IgameCooperativeLaneChangeMessage(" + header + ", " + iclm + ")"; }

		public ItsPduHeader getHeader() {
			return header;
		}

		public IgameCooperativeLaneChangeMessageBody getIclm() {
			return iclm;
		}
    }
	@Sequence
    public static class IgameCooperativeLaneChangeMessageBody {
		GenerationDeltaTime generationDeltaTime;
		IclmParameters iclmParameters;
		
		public IgameCooperativeLaneChangeMessageBody(GenerationDeltaTime generationDeltaTime,IclmParameters iclmParameters){
			this.generationDeltaTime = generationDeltaTime;
			this.iclmParameters = iclmParameters;
		}
		public IgameCooperativeLaneChangeMessageBody(){
			this(new GenerationDeltaTime(),new IclmParameters());
		}
		@Override public String toString() { return "IgameCooperativeLaneChangeMessageBody(" + generationDeltaTime + ", " + iclmParameters + ")"; }
		public GenerationDeltaTime getGenerationDeltaTime() {
			return generationDeltaTime;
		}
		public IclmParameters getIclmParameters() {
			return iclmParameters;
		}
	}
	@Sequence
    public static class IclmParameters {
		VehicleContainerHighFrequency vehicleContainerHighFrequency;
		@Asn1Optional VehicleContainerLowFrequency lowFrequencyContainer;
		MostImportantObjectContainer mostImportantObjectContainer;
		LaneObject laneObject;
		PairIdObject pairIdObject;
		MergeObject mergeObject;
		ScenarioObject scenarioObject;
		
		public IclmParameters(VehicleContainerHighFrequency vehicleContainerHighFrequency,
				VehicleContainerLowFrequency lowFrequencyContainer,
				MostImportantObjectContainer mostImportantObjectContainer,
				LaneObject laneObject,
				PairIdObject pairIdObject,
				MergeObject mergeObject,
				ScenarioObject scenarioObject){
			this.vehicleContainerHighFrequency=vehicleContainerHighFrequency;
			this.lowFrequencyContainer=lowFrequencyContainer;
			this.mostImportantObjectContainer=mostImportantObjectContainer;
			this.laneObject=laneObject;
			this.pairIdObject=pairIdObject;
			this.mergeObject=mergeObject;
			this.scenarioObject=scenarioObject;
		}
		public IclmParameters(){
			this(new VehicleContainerHighFrequency(),null,
					new MostImportantObjectContainer(),new LaneObject(),new PairIdObject(),
					new MergeObject(),new ScenarioObject());	
		}
		@Override public String toString() { return "IclmParameters(" + vehicleContainerHighFrequency + ", " 
		+ lowFrequencyContainer + ", " + mostImportantObjectContainer + ", " + laneObject + ", " 
				+ pairIdObject + ", " + mergeObject + ", " + scenarioObject + ")"; }
		public VehicleContainerHighFrequency getVehicleContainerHighFrequency() {
			return vehicleContainerHighFrequency;
		}
		public boolean hasLowFrequencyContainer() {
			return lowFrequencyContainer != null;
		}
		public VehicleContainerLowFrequency getLowFrequencyContainer() {
			return lowFrequencyContainer;
		}
		public MostImportantObjectContainer getMostImportantObjectContainer() {
			return mostImportantObjectContainer;
		}
		public LaneObject getLaneObject() {
			return laneObject;
		}
		public PairIdObject getPairIdObject() {
			return pairIdObject;
		}
		public MergeObject getMergeObject() {
			return mergeObject;
		}
		public ScenarioObject getScenarioObject() {
			return scenarioObject;
		}
	}
	@Sequence
    public static class VehicleContainerHighFrequency{
		VehicleRearAxleLocation vehicleRearAxleLocation;
		ControllerType controllerType;
		VehicleResponseTime vehicleResponseTime;
		TargetLongitudonalAcceleration targetLongitudinalAcceleration;
		TimeHeadway timeHeadway;
		CruiseSpeed cruisespeed;
		
		public VehicleContainerHighFrequency(){
			this(new VehicleRearAxleLocation(),new ControllerType(), 
					new VehicleResponseTime(), new TargetLongitudonalAcceleration(), new TimeHeadway(), new CruiseSpeed());
		}
		@Override public String toString() { return "VehicleContainerHighFrequency(" + vehicleRearAxleLocation + ", " 
				+ controllerType + ", " + vehicleResponseTime + ", " + targetLongitudinalAcceleration + ", " 
						+ timeHeadway + ", " + cruisespeed + ")"; }
		
		public VehicleContainerHighFrequency(VehicleRearAxleLocation vehicleRearAxleLocation,ControllerType controllerType,
				VehicleResponseTime vehicleResponseTime,TargetLongitudonalAcceleration targetLongitudinalAcceleration,
				TimeHeadway timeHeadway,CruiseSpeed cruisespeed){
			this.vehicleRearAxleLocation = vehicleRearAxleLocation;
			this.controllerType = controllerType;
			this.vehicleResponseTime = vehicleResponseTime;
			this.targetLongitudinalAcceleration = targetLongitudinalAcceleration;
			this.timeHeadway = timeHeadway;
			this.cruisespeed = cruisespeed;
		}

		public VehicleRearAxleLocation getVehicleRearAxleLocation() {
			return vehicleRearAxleLocation;
		}

		public ControllerType getControllerType() {
			return controllerType;
		}

		public VehicleResponseTime getVehicleResponseTime() {
			return vehicleResponseTime;
		}

		public TargetLongitudonalAcceleration getTargetLongitudinalAcceleration() {
			return targetLongitudinalAcceleration;
		}

		public TimeHeadway getTimeHeadway() {
			return timeHeadway;
		}

		public CruiseSpeed getCruisespeed() {
			return cruisespeed;
		}
	}
	@Sequence
    public static class VehicleContainerLowFrequency{
		@Asn1Optional ParticipantsReady participantsReady;
		@Asn1Optional StartPlatoon startPlatoon;
		@Asn1Optional EndOfScenario endOfScenario;
		public VehicleContainerLowFrequency(){}//Fully optional
		public VehicleContainerLowFrequency(ParticipantsReady participantsReady){this.participantsReady = participantsReady;} 
		public VehicleContainerLowFrequency(StartPlatoon startPlatoon){this.startPlatoon = startPlatoon;}
		public VehicleContainerLowFrequency(EndOfScenario endOfScenario){this.endOfScenario = endOfScenario;}
		
		@Override public String toString() { return "VehicleContainerLowFrequency("
		+" participantsReady= "+(participantsReady!= null? participantsReady : "null")
		+", startPlatoon= "+(startPlatoon!= null? startPlatoon : "null")
		+", endOfScenario= "+(endOfScenario!= null? endOfScenario : "null")
		+")";};

            public static Builder builder() { return new Builder(); }
            
		public static class Builder{
			private VehicleContainerLowFrequency val = new VehicleContainerLowFrequency();
            private boolean created = false;
            private void checkCreated() {
                if (created) { throw new IllegalStateException("Already created"); }
            }
            public VehicleContainerLowFrequency create() { created = true; return val; }
			
            public Builder participantsReady(ParticipantsReady participantsReady) { checkCreated(); val.participantsReady = participantsReady; return this; }
            public Builder startPlatoon(StartPlatoon startPlatoon) { checkCreated(); val.startPlatoon = startPlatoon; return this; }
            public Builder endOfScenario(EndOfScenario endOfScenario) { checkCreated(); val.endOfScenario = endOfScenario; return this; }
		}

		public boolean hasParticipantsReady(){
			return participantsReady!=null;
		}
		public ParticipantsReady getParticipantsReady() {
			return participantsReady;
		}
		public boolean hasStartPlatoon(){
			return startPlatoon!=null;
		}
		public StartPlatoon getStartPlatoon() {
			return startPlatoon;
		}
		public boolean hasEndOfScenario(){
			return endOfScenario!=null;
		}
		public EndOfScenario getEndOfScenario() {
			return endOfScenario;
		}
	}
	@Sequence
    public static class MostImportantObjectContainer{
		StationID mioID;
		MioRange mioRange;
		MioBearing mioBearing;
		MioRangeRate mioRangeRate;
		public MostImportantObjectContainer(){this( new StationID(), new MioRange(), new MioBearing(), new MioRangeRate() );}
		public MostImportantObjectContainer(StationID mioID,MioRange mioRange,MioBearing mioBearing,MioRangeRate mioRangeRate){
			this.mioID = mioID;
			this.mioRange = mioRange;
			this.mioBearing = mioBearing;
			this.mioRangeRate = mioRangeRate;
		}
		public StationID getMioID() {
			return mioID;
		}
		public MioRange getMioRange() {
			return mioRange;
		}
		public MioBearing getMioBearing() {
			return mioBearing;
		}
		public MioRangeRate getMioRangeRate() {
			return mioRangeRate;
		}
		
		@Override public String toString() { return "MostImportantObjectContainer(" + mioID + ", " + mioRange + ", " + mioBearing + ", " +mioRangeRate + ")"; }
	}
	@Sequence
    public static class LaneObject{
		Lane lane;
		public LaneObject(){this(new Lane());}
		public LaneObject(Lane lane){this.lane = lane;}
		public Lane getLane() {
			return lane;
		}
		@Override public String toString() { return "LaneObject(" + lane + ")"; }
	}
	
	@Sequence
    public static class PairIdObject{
		StationID forwardID;
		StationID backwardID;
		AcknowledgeFlag acknowledgeFlag;
		public PairIdObject(){this(new StationID(),new StationID(), new AcknowledgeFlag());}
		public PairIdObject(StationID forwardID, StationID backwardID, AcknowledgeFlag acknowledgeFlag){
			this.forwardID = forwardID;
			this.backwardID = backwardID;
			this.acknowledgeFlag = acknowledgeFlag;
		}
		public StationID getForwardID() {
			return forwardID;
		}
		public StationID getBackwardID() {
			return backwardID;
		}
		public AcknowledgeFlag getAcknowledgeFlag() {
			return acknowledgeFlag;
		}
		
		@Override public String toString() { return "PairIdObject(" + forwardID +", "+backwardID+", "+acknowledgeFlag+ ")"; }
	}
	
	@Sequence
    public static class MergeObject{
		MergeRequest mergeRequest;
		MergeSafeToMerge mergeSafeToMerge;
		MergeFlag mergeFlag;
		MergeFlagTail mergeFlagTail;
		MergeFlagHead mergeFlagHead;
		
		public MergeObject(){this(new MergeRequest(),new MergeSafeToMerge(),new MergeFlag(),new MergeFlagTail(),new MergeFlagHead());}
		public MergeObject(MergeRequest mergeRequest,MergeSafeToMerge mergeSafeToMerge,MergeFlag mergeFlag,MergeFlagTail mergeFlagTail,MergeFlagHead mergeFlagHead){
			this.mergeRequest = mergeRequest;
			this.mergeSafeToMerge = mergeSafeToMerge;
			this.mergeFlag = mergeFlag;
			this.mergeFlagTail = mergeFlagTail;
			this.mergeFlagHead = mergeFlagHead;
		}
		public MergeRequest getMergeRequest() {
			return mergeRequest;
		}
		public MergeSafeToMerge getMergeSafeToMerge() {
			return mergeSafeToMerge;
		}
		public MergeFlag getMergeFlag() {
			return mergeFlag;
		}
		public MergeFlagTail getMergeFlagTail() {
			return mergeFlagTail;
		}
		public MergeFlagHead getMergeFlagHead() {
			return mergeFlagHead;
		}
		@Override public String toString() { return "MergeObject(" + mergeRequest +", "+mergeSafeToMerge+", "+mergeFlag+ ", "+mergeFlagTail+", "+mergeFlagHead+ ")"; }
		
	}
	@Sequence
    public static class ScenarioObject{
		PlatoonID platoonID;
		DistanceTravelledCZ distanceTravelledCZ;
		Intention intention;
		Counter counterIntersection;
		
		public ScenarioObject(){this(new PlatoonID(), new DistanceTravelledCZ(), new Intention(), new Counter());}
		public ScenarioObject(PlatoonID platoonID,DistanceTravelledCZ distanceTravelledCZ,Intention intention,Counter counterIntersection){
			this.platoonID = platoonID;
			this.distanceTravelledCZ = distanceTravelledCZ;
			this.intention = intention;
			this.counterIntersection = counterIntersection;
		}
		public PlatoonID getPlatoonID() {
			return platoonID;
		}
		public DistanceTravelledCZ getDistanceTravelledCZ() {
			return distanceTravelledCZ;
		}
		public Intention getIntention() {
			return intention;
		}
		public Counter getCounterIntersection() {
			return counterIntersection;
		}
		@Override public String toString() { return "ScenarioObject(" + platoonID +", "+distanceTravelledCZ+", "+intention+ ", "+counterIntersection+ ")"; }
	}
	
	@Sequence
    public static class VehicleResponseTime{
		VehicleResponseTimeConstant vehicleResponseTimeConstant;
		VehicleResponseTimeDelay vehicleResponseTimeDelay;
		public VehicleResponseTime(){this(new VehicleResponseTimeConstant(),new VehicleResponseTimeDelay());}
		public VehicleResponseTime(VehicleResponseTimeConstant vehicleResponseTimeConstant,VehicleResponseTimeDelay vehicleResponseTimeDelay ){
			this.vehicleResponseTimeConstant = vehicleResponseTimeConstant;
			this.vehicleResponseTimeDelay = vehicleResponseTimeDelay;
		}
		public VehicleResponseTimeConstant getVehicleResponseTimeConstant() {
			return vehicleResponseTimeConstant;
		}
		public VehicleResponseTimeDelay getVehicleResponseTimeDelay() {
			return vehicleResponseTimeDelay;
		}
		@Override public String toString() { return "VehicleResponseTime(" + vehicleResponseTimeConstant +", "+vehicleResponseTimeDelay+")"; }
	}

	@IntRange(minValue = 0, maxValue = 4095)
    public static class VehicleRearAxleLocation extends Asn1Integer {
        public static final int ONE_METER = 100;

        public VehicleRearAxleLocation() { this(0); }
        public VehicleRearAxleLocation(int value) { super(value); }
    }
	
	@IntRange(minValue = 0, maxValue = 3)
	public static class ControllerType extends Asn1Integer {
		public static final int MANUAL = 0;
		public static final int CC = 1;
		public static final int ACC = 2;
		public static final int CACC = 3;
		
		public ControllerType() { this(MANUAL); }
		public ControllerType(int value) { super(value); }
		
		@Override public String toString() {
			switch((int)value){
			case MANUAL: return "manual";
			case CC: return "cc";
			case ACC: return "acc";
			case CACC: return "cacc";
			default: return "Unkown";
			}
		}
	}
	
	@IntRange(minValue = 0, maxValue = 1001)
	public static class VehicleResponseTimeConstant extends Asn1Integer {
		public static final int ONE_SECOND = 100;
		public static final int UNAVAILABLE = 1001;
		
		public VehicleResponseTimeConstant() { this(UNAVAILABLE); }
		public VehicleResponseTimeConstant(int value) { super(value); }
	}
	
	@IntRange(minValue = 0, maxValue = 1001)
	public static class VehicleResponseTimeDelay extends Asn1Integer {
		public static final int ONE_SECOND = 100;
		public static final int UNAVAILABLE= 1001;
		
		public VehicleResponseTimeDelay() { this(UNAVAILABLE); }
		public VehicleResponseTimeDelay(int value) { super(value); }
	}
	
	@IntRange(minValue = -1000, maxValue = 1001)
	public static class TargetLongitudonalAcceleration extends Asn1Integer {
		public static final int ONE_METER_PER_SECOND_SQUARED = 100;
		public static final int UNAVAILABLE = 1001;
		
		public TargetLongitudonalAcceleration() { this(UNAVAILABLE); }
		public TargetLongitudonalAcceleration(int value) { super(value); }
	}
	
	@IntRange(minValue = 0, maxValue = 65535)
	public static class MioRange extends Asn1Integer {
		public static final int ONE_METER = 100;
		public static final int UNAVAILABLE = 65535;
		
		public MioRange() { this(UNAVAILABLE); }
		public MioRange(int value) { super(value); }
	}
	
	@IntRange(minValue = -1571, maxValue = 1572)
	public static class MioBearing extends Asn1Integer {
		public static final int ZERO_RADIANS = 0;
		public static final int ONE_RADIANT_RIGHT = 500;
		public static final int UNAVAILABLE = 1572;
		
		public MioBearing() { this(UNAVAILABLE); }
		public MioBearing(int value) { super(value); }
	}
	
	@IntRange(minValue = -32767, maxValue = 32767)
	public static class MioRangeRate extends Asn1Integer {
		public static final int ZERO_METER_PER_SECOND = 0;
		public static final int ONE_METER_PER_SECOND = 100;
		public static final int UNAVAILABLE = 32767;
		
		public MioRangeRate() { this(UNAVAILABLE); }
		public MioRangeRate(int value) { super(value); }
	}
	
	@IntRange(minValue = 0, maxValue = 361)
	public static class TimeHeadway extends Asn1Integer {
		public static final int ONE_SECOND = 10;
		public static final int UNAVAILABLE = 361;
		
		public TimeHeadway() { this(UNAVAILABLE); }
		public TimeHeadway(int value) { super(value); }
	}
	
	@IntRange(minValue = 0, maxValue = 5001)
	public static class CruiseSpeed extends Asn1Integer {
		public static final int ONE_METER_PER_SECOND = 100;
		public static final int UNAVAILABLE = 5001;
		
		public CruiseSpeed() { this(UNAVAILABLE); }
		public CruiseSpeed(int value) { super(value); }
	}
	
	@IntRange(minValue = 0, maxValue = 1)
	public static class MergeRequest extends Asn1Integer {
		public static final int NO_MERGE_REQUEST = 0;
		public static final int mergeRequest = 1;
		
		public MergeRequest() { this(NO_MERGE_REQUEST); }
		public MergeRequest(int value) { super(value); }
		@Override public String toString(){ return value==NO_MERGE_REQUEST?"MergeRequest(no)":"MergeRequest(yes)";}
	}
	
	@IntRange(minValue = 0, maxValue = 1)
	public static class MergeSafeToMerge extends Asn1Integer {
		public static final int NOT_SAFE = 0;
		public static final int SAFE = 1;
		
		public MergeSafeToMerge() { this(NOT_SAFE); }
		public MergeSafeToMerge(int value) { super(value); }
		
		@Override public String toString(){ return value==NOT_SAFE?"MergeSafeToMerge(not safe)":"MergeSafeToMerge(safe)";}
	}
	
	@IntRange(minValue = 0, maxValue = 1)
	public static class MergeFlag extends Asn1Integer {
		public static final int NOT_MERGE_READY = 0;
		public static final int MERGE_READY = 1;
		
		public MergeFlag() { this(NOT_MERGE_READY); }
		public MergeFlag(int value) { super(value); }
	}
	
	@IntRange(minValue = 0, maxValue = 1)
	public static class MergeFlagTail extends Asn1Integer {
		public static final int NOT_LAST_VEHICLE = 0;
		public static final int LAST_VEHICLE = 1;
		
		public MergeFlagTail() { this(NOT_LAST_VEHICLE); }
		public MergeFlagTail(int value) { super(value); }
	}
	
	@IntRange(minValue = 0, maxValue = 1)
	public static class MergeFlagHead extends Asn1Integer {
		public static final int NOT_FIRST_VEHICLE = 0;
		public static final int FIRST_VEHICLE = 1;
		
		public MergeFlagHead() { this(NOT_FIRST_VEHICLE); }
		public MergeFlagHead(int value) { super(value); }
	}
	
	@IntRange(minValue = 0, maxValue = 255)
	public static class PlatoonID extends Asn1Integer {
		public static final int PLATOON_A = 1;
		public static final int PLATOON_B = 2;
		public static final int NOT_USED = 3;
		
		public PlatoonID() { this(NOT_USED); }
		public PlatoonID(int value) { super(value); }
	}
	
	@IntRange(minValue = 0, maxValue = 10000)
	public static class DistanceTravelledCZ extends Asn1Integer {
		public static final int ONE_METER = 10;
		
		public DistanceTravelledCZ() { this(0); }
		public DistanceTravelledCZ(int value) { super(value); }
	}

	@IntRange(minValue = 1, maxValue = 3)
	public static class Intention extends Asn1Integer {
		public static final int STRAIGHT_NO_TURNING = 1;
		public static final int TURN_LEFT = 2;
		public static final int TURN_RIGHT = 3;
		
		public Intention() { this(STRAIGHT_NO_TURNING); }
		public Intention(int value) { super(value); }
	}
	
	@IntRange(minValue = 1, maxValue = 4)
	public static class Lane extends Asn1Integer {
		public static final int LANE_ONE = 1;
		public static final int LANE_TWO = 2;
		public static final int LANE_THREE = 3;
		public static final int UNAVAILABLE = 4;
		
		public Lane() { this(UNAVAILABLE); }
		public Lane(int value) { super(value); }
	}

	@IntRange(minValue = 0, maxValue = 3)
	public static class Counter extends Asn1Integer {
		public static final int NO_VEHICLES = 0;
		public static final int ONE_VEHICLE = 1;
		
		public Counter() { this(NO_VEHICLES); }
		public Counter(int value) { super(value); }
	}

	@IntRange(minValue = 0, maxValue = 1)
	public static class AcknowledgeFlag extends Asn1Integer {
		public static final int ACKNOWLEDGED = 1;
		public static final int NOT_ACKNOWLEDGED = 0;
		
		public AcknowledgeFlag() { this(ACKNOWLEDGED); }
		public AcknowledgeFlag(int value) { super(value); }
	}
	
	@IntRange(minValue = 0, maxValue = 1)
	public static class ParticipantsReady extends Asn1Integer {
		public static final int NOT_READY = 0;
		public static final int READY  =1;
		
		public ParticipantsReady() { this(NOT_READY); }
		public ParticipantsReady(int value) { super(value); }
	}
	
	@IntRange(minValue = 0, maxValue = 1)
	public static class StartPlatoon extends Asn1Integer {
		public static final int START_PLATOON_A_AT_SPEED_80KPH = 0;
		public static final int START_PLATOON_B_AT_SPEED_60KPH = 1;
		
		public StartPlatoon() { this(START_PLATOON_A_AT_SPEED_80KPH); }
		public StartPlatoon(int value) { super(value); }
	}
	
	@IntRange(minValue = 1, maxValue = 1)
	public static class EndOfScenario extends Asn1Integer {
		public static final int END_OF_SCENRIO = 1;
		
		public EndOfScenario() { this(END_OF_SCENRIO); }
		public EndOfScenario(int value) { super(value); }
	}
	
}

